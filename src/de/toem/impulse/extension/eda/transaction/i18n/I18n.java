package de.toem.impulse.extension.eda.transaction.i18n;

public class I18n extends de.toem.impulse.i18n.I18n { 

    public static String Serializer_ScvReader="SCV Reader";
    public static String Serializer_ScvReader_Description="With the SCV reader you can read System-C transaction text database traces. A SCV text database file stores transaction traces in plain text format.";
    public static String Serializer_ScvReader_IconId=null;
    public static String Serializer_ScvReader_HelpURL="impulse-extension/${BND}/scv-reader";
    
    public static String Serializer_FtrReader="FTR Reader";
    public static String Serializer_FtrReader_Description="With the FTR reader you can read Fast Transaction Recording files from SystemC TLM simulations. An FTR file stores transaction data in a compact binary format using CBOR.";
    public static String Serializer_FtrReader_IconId=null;
    public static String Serializer_FtrReader_HelpURL="impulse-extension/${BND}/ftr-reader";

    public static String Processor_TlmPhaseAnalyzer="TLM Phase Analyzer";
    public static String Processor_TlmPhaseAnalyzer_Description="The TLM Phase Analyzer processes Transaction-Level Modeling (TLM) traces to analyze phases and timing in SystemC simulations.";
    public static String Processor_TlmPhaseAnalyzer_IconId=null;
    public static String Processor_TlmPhaseAnalyzer_HelpURL=null;//"impulse-extension/${BND}/tlm-phase-analyzer";

    public static String Processor_Axi3Analyzer="AXI3 Analyzer";
    public static String Processor_Axi3Analyzer_Description="The AXI3 Analyzer examines AXI3 bus protocol transactions to detect issues, measure performance, and visualize data flow in embedded systems.";
    public static String Processor_Axi3Analyzer_IconId=null;
    public static String Processor_Axi3Analyzer_HelpURL=null;//"impulse-extension/${BND}/axi3-analyzer";
}
