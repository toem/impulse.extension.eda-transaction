package de.toem.impulse.extension.eda.transaction.tlm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.toem.impulse.ImpulseBase;
import de.toem.impulse.cells.processor.JavaSamplesProcessorPreference;
import de.toem.impulse.extension.eda.transaction.i18n.I18n;
import de.toem.impulse.samples.IMemberDescriptor;
import de.toem.impulse.samples.IReadableSample;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamplePointer;
import de.toem.impulse.samples.ISamplePointerIterator;
import de.toem.impulse.samples.ISamples;
import de.toem.impulse.samples.IStructSamplesWriter;
import de.toem.impulse.samples.domain.IDomainBase;
import de.toem.impulse.samples.processor.ISamplesProcessorDescriptor;
import de.toem.impulse.samples.raw.Enumeration;
import de.toem.impulse.samples.raw.StructMember;
import de.toem.impulse.usecase.eda.transaction.AbstractTransactionAnalyzer;
import de.toem.impulse.usecase.eda.transaction.ITransaction.PhasesListener;
import de.toem.toolkits.core.Utils;
import de.toem.toolkits.pattern.bundles.Bundles;
import de.toem.toolkits.pattern.element.ICell;
import de.toem.toolkits.pattern.registry.RegistryAnnotation;
import de.toem.toolkits.pattern.threading.IProgress;
import de.toem.toolkits.utils.text.MultilineText;

/**
 * TLM Phase Analyzer implementation for impulse.
 *
 * This analyzer processes transaction-level data from SystemC TLM simulations, identifying and correlating transaction phases
 * such as BEGIN_REQ, END_REQ, BEGIN_RESP, END_RESP, and their partial variants. It extracts protocol-specific attributes
 * like burst, cache, and protection settings for AXI transactions.
 *
 * Key features implemented by this analyzer:
 * - TLM phase detection and correlation using transaction UIDs
 * - Support for AXI3, AXI4, and ACE protocol attributes
 * - Partial transaction handling for complex TLM flows
 * - Delay compensation based on transaction delay attributes
 * - Hierarchical transaction structure creation with request/response pairing
 * - Protocol-specific attribute extraction (burst, cache, protection)
 * - Return path phase handling for bidirectional TLM communications
 *
 * The analyzer processes transaction streams with tlm_phase attributes, creating structured transaction records
 * that can be used for protocol analysis, performance metrics, and visualization in impulse.
 *
 * Implementation Notes:
 * - Uses transaction UIDs (trans.uid or trans.ptr) for correlation
 * - Supports both forward and return path phases
 * - Handles delay compensation for accurate timing
 * - Extracts AXI-specific attributes when available
 * - Creates pending transaction tracking for phase correlation
 *
 * Copyright (c) 2013-2025 Thomas Haber All rights reserved.
 *
 * @see de.toem.impulse.usecase.eda.transaction.AbstractTransactionAnalyzer
 */
@RegistryAnnotation(annotation = TlmPhaseAnalyzer.Annotation.class)
public class TlmPhaseAnalyzer extends AbstractTransactionAnalyzer  {

    /**
     * Annotation metadata for the TLM Phase Analyzer.
     */
    public static class Annotation {

        public static final String id = "de.toem.impulse.processor.tlmPhaseAnalyzer";
        public static final String label = I18n.Processor_TlmPhaseAnalyzer;
        public static final String description = I18n.Processor_TlmPhaseAnalyzer_Description;
        public static final String iconId = I18n.Processor_TlmPhaseAnalyzer_IconId;
        public static final String helpURL = I18n.Processor_TlmPhaseAnalyzer_HelpURL;
        public static final String certificate = "AHDwWg+h5S9gINylwjBesL6AN4gUuZ0D\nVhODxCEfY7ExnE2ylazpEwuuq2EVmdJT\n7lJr9w7ExvUU6uVBl8aJVTRGMZxjJjns\nbKheyNBa3fhpaBVBQ4zo5Qvsi7FQumPd\nS4WDVuiBTUDK6/K23Vd4JBETtAegBG12\npEoaVokZbyfN8n+x6wMQ4CcGcxWOA9LY\nuIhjJH3o8OxpgsHjUp4vFR3QGmwOna0d\nETtv1pK8dv2TUx6u5nwdrPvIGJM5Dsvg\n8mTDXHoQhbd9HWTIvt3CF+8AWGN/04rH\nUDSiSeEwC36aCSh8aJakPX55gE9/PiLi\nAMucNGJ4pAvTTWuEE2GmOOZpjqpbbPE7\n2hytgWddFgjFhvR4+hbLDCPq/4Pscx2V\nDIHHprMVbo0=\n";
    }

    

    // ========================================================================================================================
    // Constructors
    // ========================================================================================================================

    /**
     * Default constructor for the TLM Phase Analyzer.
     */
    public TlmPhaseAnalyzer() {
        super();
    }

    /**
     * Fully parameterized constructor for the TLM Phase Analyzer.
     *
     * @param descriptor
     *            The samples processor descriptor providing contextual information
     * @param configuration
     *            Configuration name for specialized settings
     * @param id
     *            Unique identifier for this processor instance
     * @param label
     *            Display label for this processor
     * @param description
     *            Description of this processor's functionality
     * @param tags
     *            Tags associated with this processor
     * @param sources
     *            Map of source samples for processing
     * @param properties
     *            Additional properties as key-value pairs
     * @param modes
     *            Processing modes supported by this analyzer
     */
    public TlmPhaseAnalyzer(ISamplesProcessorDescriptor descriptor, String configuration, String id, String label, String description, String tags, Map<Object, IReadableSamples> sources, String[][] properties, int modes) {
        super(descriptor, configuration, id, label, description, tags, sources, properties, modes & MODE_MAIN_SLAVE_PROCESSING);
    }

    // ========================================================================================================================
    // Support Interface
    // ========================================================================================================================

    /**
     * Determines if this analyzer supports the specified functionality request.
     *
     * @param request
     *            An Integer identifying the functionality being queried
     * @param context
     *            Additional context for the request
     * @return true if the analyzer supports the requested functionality, false otherwise
     */
    public static boolean supports(Object request, Object context) {
        int ir = request instanceof Integer ? ((Integer) request).intValue() : -1;
        return AbstractTransactionAnalyzer.supports(request, context) ||  ir == (ir & (SUPPORT_OPTIONAL_MAIN_PROCESSING | SUPPORT_SOURCE));
    }

    /**
     * Creates a Java preference for this analyzer.
     *
     * @return The Java preference cell.
     */
    public static ICell createJavaPreference() {
        try {
        JavaSamplesProcessorPreference p =  new  JavaSamplesProcessorPreference();
        p.setName(Annotation.label);
        p.description = Annotation.description;
        p.helpUrl = Annotation.helpURL;
        p.certificate = Annotation.certificate;
        p.impl = MultilineText.toXml(Bundles.getBundleSourceEntryAsString(TlmPhaseAnalyzer.class));
        p.javaBundle = Utils.commarize(ImpulseBase.BUNDLE_ID,Bundles.getBundleId(TlmPhaseAnalyzer.class)); 
        return p;
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * Determines if this analyzer is the default choice for the given context.
     *
     * @param context
     *            The context to evaluate
     * @return true if this analyzer should be the default for the context
     */
    public static boolean isDefault(Object context) {
        if (context instanceof IReadableSamples) {
            if (((IReadableSamples) context).getMemberDescriptor("tlm_phase") == null)
                // no tlm_phase member, so no TLM phase
                return false;
            return true;
        }
        return false;
    }    

    // ========================================================================================================================
    // Additional Members
    // ========================================================================================================================

    /**
     * Creates additional struct members for the specified signal type.
     *
     * @param signal
     *            The signal type (SIGNAL_TRANSACTIONS or SIGNAL_REQUESTS)
     * @param writer
     *            The struct samples writer
     * @return List of additional struct members
     */
    @Override
    protected List<StructMember> createAdditionalMembers(int signal, IStructSamplesWriter writer) {
        List<StructMember> members = new ArrayList<>();
        if (signal == SIGNAL_TRANSACTIONS || signal == SIGNAL_REQUESTS) {
            members.add(writer.createMember(null, "Burst", null, null, ISamples.TAG_STATE, StructMember.DATA_TYPE_INTEGER, -1, ISample.FORMAT_DEFAULT));
            members.add(writer.createMember(null, "Cache", null, null, ISamples.TAG_STATE, StructMember.DATA_TYPE_INTEGER, -1, ISample.FORMAT_DEFAULT));
            members.add(writer.createMember(null, "Prot", null, null, ISamples.TAG_STATE, StructMember.DATA_TYPE_INTEGER, -1, ISample.FORMAT_DEFAULT));
        }
        return members;
    }

    /**
     * Sets the values of additional struct members.
     *
     * @param signal
     *            The signal type
     * @param members
     *            Array of struct members
     * @param additionalRequest
     *            Additional request data
     * @param additionalResponse
     *            Additional response data
     */
    @Override
    protected void setAdditionalMembers(int signal, StructMember[] members, Object[] additionalRequest, Object[] additionalResponse) {
        if (signal == SIGNAL_TRANSACTIONS || signal == SIGNAL_REQUESTS) {
            members[MEMBER_ADDITIONAL + 0].setValue(additionalRequest[0]);
            members[MEMBER_ADDITIONAL + 1].setValue(additionalRequest[1]);
            members[MEMBER_ADDITIONAL + 2].setValue(additionalRequest[2]);
        }
    } 

    // ========================================================================================================================
    // Find phases
    // ========================================================================================================================

    /**
     * Finds and processes TLM transaction phases from the sample iterator.
     *
     * This method analyzes transaction samples to identify TLM phase transitions, correlate requests and responses
     * using transaction UIDs, and extract protocol-specific attributes. It handles both forward and return path phases,
     * supports partial transactions, and applies delay compensation.
     *
     * @param p
     *            Progress indicator for long-running operations
     * @param iter
     *            Iterator over sample pointers
     * @param pointers
     *            Map of sample pointers by key
     * @param listener
     *            Listener for phase events
     * @param pending
     *            Map of pending transactions by UID
     * @return true if phases were successfully found and processed
     */
    @Override
    public boolean findPhases(IProgress p, ISamplePointerIterator iter, Map<Object,ISamplePointer> pointers, PhasesListener<Pending> listener, Map<Long, Pending> pending) {
        
        // get first value of map that is != null
        ISamplePointer pointer = pointers != null ? pointers.values().stream().filter(pn -> pn != null).findFirst().orElse(null) : null;
        if (pointer == null)
            return false;
            
        console.info("TlmPhaseDeducer.findPhases()");
        IMemberDescriptor member_uid = pointer.getMemberDescriptor("trans.uid");
        if (member_uid == null)
            member_uid = pointer.getMemberDescriptor("trans.ptr");
        IMemberDescriptor member_cmd = pointer.getMemberDescriptor("trans.cmd");
        IMemberDescriptor member_address = pointer.getMemberDescriptor("trans.address");
        IMemberDescriptor member_data_length = pointer.getMemberDescriptor("trans.data_length");
        IMemberDescriptor member_delay = pointer.getMemberDescriptor("delay");
        IMemberDescriptor member_tlm_phase = pointer.getMemberDescriptor("tlm_phase");
        Enumeration enum_tlm_phase_begin_req = pointer.getMemberEnum(member_tlm_phase, "BEGIN_REQ");
        Enumeration enum_tlm_phase_end_req = pointer.getMemberEnum(member_tlm_phase, "END_REQ");
        Enumeration enum_tlm_phase_begin_resp = pointer.getMemberEnum(member_tlm_phase, "BEGIN_RESP");
        Enumeration enum_tlm_phase_end_resp = pointer.getMemberEnum(member_tlm_phase, "END_RESP");
        Enumeration enum_tlm_phase_begin_partial_req = pointer.getMemberEnum(member_tlm_phase, "BEGIN_PARTIAL_REQ");
        Enumeration enum_tlm_phase_end_partial_req = pointer.getMemberEnum(member_tlm_phase, "END_PARTIAL_REQ");
        Enumeration enum_tlm_phase_begin_partial_resp = pointer.getMemberEnum(member_tlm_phase, "BEGIN_PARTIAL_RESP");
        Enumeration enum_tlm_phase_end_partial_resp = pointer.getMemberEnum(member_tlm_phase, "END_PARTIAL_RESP");
        IMemberDescriptor member_tlm_phase_ret = pointer.getMemberDescriptor("tlm_phase[return_path]");
        Enumeration enum_tlm_phase_ret_begin_req = pointer.getMemberEnum(member_tlm_phase_ret, "BEGIN_REQ");
        Enumeration enum_tlm_phase_ret_end_req = pointer.getMemberEnum(member_tlm_phase_ret, "END_REQ");
        Enumeration enum_tlm_phase_ret_begin_resp = pointer.getMemberEnum(member_tlm_phase_ret, "BEGIN_RESP");
        Enumeration enum_tlm_phase_ret_end_resp = pointer.getMemberEnum(member_tlm_phase_ret, "END_RESP");
        Enumeration enum_tlm_phase_ret_begin_partial_req = pointer.getMemberEnum(member_tlm_phase_ret, "BEGIN_PARTIAL_REQ");
        Enumeration enum_tlm_phase_ret_end_partial_req = pointer.getMemberEnum(member_tlm_phase_ret, "END_PARTIAL_REQ");
        Enumeration enum_tlm_phase_ret_begin_partial_resp = pointer.getMemberEnum(member_tlm_phase_ret, "BEGIN_PARTIAL_RESP");
        Enumeration enum_tlm_phase_ret_end_partial_resp = pointer.getMemberEnum(member_tlm_phase_ret, "END_PARTIAL_RESP");
        IMemberDescriptor member_length = null;
        IMemberDescriptor member_burst = null;
        IMemberDescriptor member_cache = null;
        IMemberDescriptor member_prot = null;
        IMemberDescriptor member_id = pointer.getMemberDescriptor("trans.axi3.id");
        if (member_id != null) {
            member_length = pointer.getMemberDescriptor("trans.axi3.length");
            member_burst = pointer.getMemberDescriptor("trans.axi3.burst");
            member_cache = pointer.getMemberDescriptor("trans.axi3.cache");
            member_prot = pointer.getMemberDescriptor("trans.axi3.prot");
        } else {
            member_id = pointer.getMemberDescriptor("trans.axi4.id");
            if (member_id != null) {
                member_length = pointer.getMemberDescriptor("trans.axi4.length");
                member_burst = pointer.getMemberDescriptor("trans.axi4.burst");
                member_cache = pointer.getMemberDescriptor("trans.axi4.cache");
                member_prot = pointer.getMemberDescriptor("trans.axi4.prot");
            } else {
                member_id = pointer.getMemberDescriptor("trans.ace.id");
                if (member_id != null) {
                    member_length = pointer.getMemberDescriptor("trans.ace.length");
                    member_burst = pointer.getMemberDescriptor("trans.ace.burst");
                    member_cache = pointer.getMemberDescriptor("trans.ace.cache");
                    member_prot = pointer.getMemberDescriptor("trans.ace.prot");
                } else {
                }
            }
        }
        // delays
        Map<Enumeration, Long> delays = new HashMap<>();
        if (member_delay != null) {
            List<Enumeration> enums = pointer.getMemberEnums(member_delay);
            for (Enumeration e : enums) {
                Number n = pointer.getDomainBase().parseMultiple(e.label, IDomainBase.PARSE_ANY, null);
                delays.put(e, n != null ? n.longValue() : 0);
                //console.info("Found delays", e.label, "=", delays.get(e));
            }
        }
        while (iter.hasNext()) {
            long current = iter.next();
            IReadableSample sample = pointer.compound();
            if (sample == null)
                continue;
            long uid = sample.longValueOf(member_uid);
            Enumeration tlm_phase = sample.enumValueOf(member_tlm_phase);
            Enumeration tlm_phase_ret = sample.enumValueOf(member_tlm_phase_ret);
            Enumeration delay_enum = sample.enumValueOf(member_delay);
            Long delay = delays.get(delay_enum);
            if (delay != null)
                current += delay;
            if (uid != 0 && tlm_phase != null) {
                // phases
                if (Utils.equals(enum_tlm_phase_begin_req, tlm_phase)) {
                    String cmd = sample.formatOf(member_cmd,ISample.FORMAT_LABEL);
                    long address = sample.longValueOf(member_address);
                    int dataLength = sample.intValueOf(member_data_length);
                    int burst = sample.intValueOf(member_burst);
                    int cache = sample.intValueOf(member_cache);
                    int prot = sample.intValueOf(member_prot);
                    beginReq(current, uid, false, 0, sample, Utils.equals(enum_tlm_phase_ret_end_req, tlm_phase_ret), pending, cmd, address, dataLength, /*additional*/burst, cache, prot);
                } else if (Utils.equals(enum_tlm_phase_end_req, tlm_phase)) {
                    endReq(current, uid, sample, pending);
                } else if (Utils.equals(enum_tlm_phase_begin_resp, tlm_phase)) {
                    beginResp(current, uid, false, 0, sample, Utils.equals(enum_tlm_phase_ret_end_resp, tlm_phase_ret), pending);
                } else if (Utils.equals(enum_tlm_phase_end_resp, tlm_phase)) {
                    endResp(current, uid, sample, pending);
                } else if (Utils.equals(enum_tlm_phase_begin_partial_req, tlm_phase)) {
                    String cmd = sample.formatOf(member_cmd,ISample.FORMAT_LABEL);
                    long address = sample.longValueOf(member_address);
                    int dataLength = sample.intValueOf(member_data_length);
                    int burst = sample.intValueOf(member_burst);
                    int cache = sample.intValueOf(member_cache);
                    int prot = sample.intValueOf(member_prot);
                    beginReq(current, uid, true, 0, sample, Utils.equals(enum_tlm_phase_ret_end_partial_req, tlm_phase_ret), pending, cmd, address, dataLength, /*additional*/burst, cache, prot);
                } else if (Utils.equals(enum_tlm_phase_end_partial_req, tlm_phase)) {
                    endReq(current, uid, sample, pending);
                } else if (Utils.equals(enum_tlm_phase_begin_partial_resp, tlm_phase)) {
                    beginResp(current, uid, true, 0, sample, Utils.equals(enum_tlm_phase_ret_end_partial_resp, tlm_phase_ret), pending);
                } else if (Utils.equals(enum_tlm_phase_end_partial_resp, tlm_phase)) {
                    endResp(current, uid, sample, pending);
                }
            }
        }
        return true;
    }
}
