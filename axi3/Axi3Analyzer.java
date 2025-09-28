package de.toem.impulse.extension.eda.transaction.axi3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.toem.impulse.ImpulseBase;
import de.toem.impulse.cells.processor.JavaSamplesProcessorPreference;
import de.toem.impulse.extension.eda.transaction.i18n.I18n;
import de.toem.impulse.samples.IReadableSample;
import de.toem.impulse.samples.IReadableSamples;
import de.toem.impulse.samples.ISample;
import de.toem.impulse.samples.ISamplePointer;
import de.toem.impulse.samples.ISamplePointerIterator;
import de.toem.impulse.samples.ISamples;
import de.toem.impulse.samples.IStructSamplesWriter;
import de.toem.impulse.samples.processor.ISamplesProcessorDescriptor;
import de.toem.impulse.samples.processor.ISamplesProcessorDescriptor.ISourceHandler;
import de.toem.impulse.samples.raw.StructMember;
import de.toem.impulse.usecase.eda.transaction.AbstractTransactionAnalyzer;
import de.toem.impulse.usecase.eda.transaction.ITransaction.PhasesListener;
import de.toem.toolkits.core.Utils;
import de.toem.toolkits.pattern.bundles.Bundles;
import de.toem.toolkits.pattern.element.ICell;
import de.toem.toolkits.pattern.ide.IConsoleStream;
import de.toem.toolkits.pattern.registry.RegistryAnnotation;
import de.toem.toolkits.pattern.threading.IProgress;
import de.toem.toolkits.utils.text.MultilineText;

@RegistryAnnotation(annotation = Axi3Analyzer.Annotation.class)
public class Axi3Analyzer extends AbstractTransactionAnalyzer {

    public static class Annotation {

        public static final String id = "de.toem.impulse.processor.axi3Anaylzer";
        public static final String label = I18n.Processor_Axi3Analyzer;
        public static final String description = I18n.Processor_Axi3Analyzer_Description;
        public static final String iconId = I18n.Processor_Axi3Analyzer_IconId;
        public static final String helpURL = I18n.Processor_Axi3Analyzer_HelpURL;
        public static final String certificate = "EXOW/TZFRzBniMoIQkqOppGz0W/pWyag\nVhODxCEfY7ExnE2ylazpEwuuq2EVmdJT\nn1hbv0V4e7MU6uVBl8aJVQ3rZ60fDtS7\nui1spOtNmjQQTHNe2vOlSU+MTR3zkBn7\nvZD/b9iKtrCsMz0p4WqogVxE0asSlh9z\n+CvLbF2KBzrsa2pteVjkgTiX/rSMmgF3\nGq74h8bXSUAfVc2HeKri4Cx4ohQvdpiM\nPTUtKUHghTwVxNvp+KxlCLr1TSqHCn7V\nf3GyJ+bkFEjW99qmjULY9MqEk+g0GnNw\nccz76VLwGqWuK9kdt/g1j04Bs/4D32El\n9243TMWsh6i1pghxWWRdlVM8Nez7NTws\nWAfW4orFOh4EIO4RKPvlXw==\n";
    }

    // ========================================================================================================================
    // Constants
    // ========================================================================================================================
    
    static final String ACLK = "ACLK";
    static final String ARESETn = "ARESETn";
    static final String AWID = "AWID";
    static final String AWADDR = "AWADDR";
    static final String AWLEN = "AWLEN";
    static final String AWSIZE = "AWSIZE";
    static final String AWBURST = "AWBURST";
    static final String AWLOCK = "AWLOCK";
    static final String AWCACHE = "AWCACHE";
    static final String AWPROT = "AWPROT";
    static final String AWQOS = "AWQOS";
    static final String AWVALID = "AWVALID";
    static final String AWREADY = "AWREADY";
    static final String WDATA = "WDATA";
    static final String WSTRB = "WSTRB";
    static final String WLAST = "WLAST";
    static final String WVALID = "WVALID";
    static final String WREADY = "WREADY";
    static final String BID = "BID";
    static final String BRESP = "BRESP";
    static final String BVALID = "BVALID";
    static final String BREADY = "BREADY";
    static final String ARID = "ARID";
    static final String ARADDR = "ARADDR";
    static final String ARLEN = "ARLEN";
    static final String ARSIZE = "ARSIZE";
    static final String ARBURST = "ARBURST";
    static final String ARLOCK = "ARLOCK";
    static final String ARCACHE = "ARCACHE";
    static final String ARPROT = "ARPROT";
    static final String ARQOS = "ARQOS";
    static final String ARVALID = "ARVALID";
    static final String ARREADY = "ARREADY";
    static final String RID = "RID";
    static final String RDATA = "RDATA";
    static final String RRESP = "RRESP";
    static final String RLAST = "RLAST";
    static final String RVALID = "RVALID";
    static final String RREADY = "RREADY";

    static String[][] AXI3_SOURCES = new String[][] { { ACLK, rx("aclk", "axi.*clk") },
            { ARESETn, rx("aresetn", "resetn", "rst_n", "rstn", "axi.*resetn") }, { AWID, rx("awid") }, { AWADDR, rx("awaddr") },
            { AWLEN, rx("awlen") }, { AWSIZE, rx("awsize") }, { AWBURST, rx("awburst") }, { AWLOCK, rx("awlock") },
            { AWCACHE, rx("awcache") }, { AWPROT, rx("awprot") }, { AWQOS, rx("awqos") }, { AWVALID, rx("awvalid") },
            { AWREADY, rx("awready") }, { WDATA, rx("wdata") }, { WSTRB, rx("wstrb") }, { WLAST, rx("wlast") }, { WVALID, rx("wvalid") },
            { WREADY, rx("wready") }, { BID, rx("bid") }, { BRESP, rx("bresp") }, { BVALID, rx("bvalid") }, { BREADY, rx("bready") },
            { ARID, rx("arid") }, { ARADDR, rx("araddr") }, { ARLEN, rx("arlen") }, { ARSIZE, rx("arsize") }, { ARBURST, rx("arburst") },
            { ARLOCK, rx("arlock") }, { ARCACHE, rx("arcache") }, { ARPROT, rx("arprot") }, { ARQOS, rx("arqos") },
            { ARVALID, rx("arvalid") }, { ARREADY, rx("arready") }, { RID, rx("rid") }, { RDATA, rx("rdata") }, { RRESP, rx("rresp") },
            { RLAST, rx("rlast") }, { RVALID, rx("rvalid") }, { RREADY, rx("rready") } };

    private static String rx(String... alts) {
        return "(?i).*(?:" + String.join("|", alts) + ").*";
    }

    // ========================================================================================================================
    // Constructors
    // ========================================================================================================================

    public Axi3Analyzer() {
        super();
    }

    public Axi3Analyzer(ISamplesProcessorDescriptor descriptor, String configuration, String id, String label, String description, String tags,
            Map<Object, IReadableSamples> sources, String[][] properties, int modes) {
        super(descriptor, configuration, id, label, description, tags, sources, properties, modes & MODE_MAIN_SLAVE_PROCESSING);
    }

    // ========================================================================================================================
    // Support Interface
    // ========================================================================================================================

    /**
     * Determines if this reader supports the specified functionality request.
     *
     * @param request
     *            An Integer identifying the functionality being queried
     * @param context
     *            Additional context for the request
     * @return true if the reader supports the requested functionality, false otherwise
     */
    public static boolean supports(Object request, Object context) {
        int ir = request instanceof Integer ? ((Integer) request).intValue() : -1;
        if (Utils.equals(SUPPORT_DEFAULT, request) && context instanceof IReadableSamples) {
            // if (((IReadableSamples) context).getMemberDescriptor("tlm_phase") == null)
            // no tlm_phase member, so no TLM phase
            return false;
            // return true;
        }
        return AbstractTransactionAnalyzer.supports(request, context)
                || ir == (ir & (SUPPORT_SOURCE | SUPPORT_MAIN_PROCESSING | SUPPORT_MULTIPLE_SOURCES | SUPPORT_AUTO_SOURCES));
    }

    /**
     * Creates a Java preference for this reader.
     * 
     * @return The Java preference cell.
     */
    public static ICell createJavaPreference() {
        try {
            JavaSamplesProcessorPreference p = new JavaSamplesProcessorPreference();
            p.setName(Annotation.label);
            p.description = Annotation.description;
            p.helpUrl = Annotation.helpURL;
            p.certificate = Annotation.certificate;
            p.impl = MultilineText.toXml(Bundles.getBundleSourceEntryAsString(Axi3Analyzer.class));
            p.javaBundle = Utils.commarize(ImpulseBase.BUNDLE_ID,Bundles.getBundleId(Axi3Analyzer.class));
            return p;
        } catch (Throwable e) {
        }
        return null;
    }

    public static boolean isDefault(Object context) {
        if (context instanceof IReadableSamples) {

        }
        return false;
    }

    // ========================================================================================================================
    // Auto sources
    // ========================================================================================================================

    public static boolean updateAutoSources(Map<ICell, ICell> cells, Map<Object, IReadableSamples> sources, ISourceHandler sourceHandler) {

        // get primary
        ICell view = cells.keySet().iterator().next();
        ICell primary = cells.get(view);
        if (primary == null)
            primary = view;
        // extract names of all primary children
        List<String> available = primary.getChildren().stream().map(ICell::getName).collect(Collectors.toList());

        // detect sources
        for (String[] sourceName : AXI3_SOURCES) {

            if (sources.containsKey(sourceName))
                continue;
            IReadableSamples source = detectSource(sourceName[0], sourceName[1], primary, available, sourceHandler);
            if (source != null)
                sources.put(sourceName, source);
        }

        return true;
    }

    private static IReadableSamples detectSource(String sourceName, String regEx, ICell primary, List<String> available,
            ISourceHandler sourceHandler) {

        ICell cell = null;
        for (String n : available) {
            if (n.equals(sourceName)) {
                cell = primary.getChild(n);
            }
        }
        if (regEx != null)
            for (String n : available) {
                if (n.matches(regEx)) {
                    cell = primary.getChild(n);
                }
            }
        else
            for (String n : available) {
                if (n.equalsIgnoreCase(sourceName) || n.toLowerCase().contains(sourceName.toLowerCase())) {
                    cell = primary.getChild(n);
                }
            }
        return cell != null ? sourceHandler.getSource(cell) : null;
    }

    // ========================================================================================================================
    // Additional Members
    // ========================================================================================================================

    @Override
    protected List<StructMember> createAdditionalMembers(int signal, IStructSamplesWriter writer) {
        List<StructMember> members = new ArrayList<>();
        if (signal == SIGNAL_TRANSACTIONS || signal == SIGNAL_REQUESTS) {
            members.add(
                    writer.createMember(null, "Burst", null, null, ISamples.TAG_STATE, StructMember.DATA_TYPE_INTEGER, -1, ISample.FORMAT_DEFAULT));
            members.add(
                    writer.createMember(null, "Cache", null, null, ISamples.TAG_STATE, StructMember.DATA_TYPE_INTEGER, -1, ISample.FORMAT_DEFAULT));
            members.add(
                    writer.createMember(null, "Prot", null, null, ISamples.TAG_STATE, StructMember.DATA_TYPE_INTEGER, -1, ISample.FORMAT_DEFAULT));
        }
        return members;
    }

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
    @Override
    public boolean findPhases(IProgress p, IConsoleStream console, ISamplePointerIterator iter, Map<Object,ISamplePointer> pointers, PhasesListener<Pending> listener, Map<Long, Pending> pending) {
        
        // get first value of map that is != null
        ISamplePointer pointer = pointers != null ? pointers.values().stream().filter(pn -> pn != null).findFirst().orElse(null) : null;
        if (pointer == null)
            return false;
        
        console.info("Axi3Deducer.findPhases()");

        while (iter.hasNext()) {
            long current = iter.next();
            IReadableSample sample = pointer.compound();
            if (sample == null)
                continue;
            targetWriter.writeSample(sample);
        }
        return true;
    }
}
