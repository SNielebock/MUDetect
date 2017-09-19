package de.tu_darmstadt.stg.mudetect.aug.dot;

import com.google.common.io.Files;
import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.APIUsageGraph;
import de.tu_darmstadt.stg.mudetect.aug.Edge;
import de.tu_darmstadt.stg.mudetect.aug.Node;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.VertexNameProvider;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AUGDotExporter {
    private static final String WINDOWS_EXEC_DOT = "D:/Program Files (x86)/Graphviz2.36/bin/dot.exe";	// Windows
    private static final String LINUX_EXEC_DOT = "/usr/local/bin/dot";	// Linux
    private static String EXEC_DOT = null;

    static {
        if (System.getProperty("os.name").startsWith("Windows"))
            EXEC_DOT = WINDOWS_EXEC_DOT;
        else
            EXEC_DOT = LINUX_EXEC_DOT;
    }

    private static final String NEW_LINE = System.getProperty("line.separator");

    private final IntegerNameProvider<Node> nodeIdProvider = new IntegerNameProvider<>();
    private final DOTExporter<Node, Edge> exporter;

    public AUGDotExporter(VertexNameProvider<Node> nodeLabelProvider,
                          AUGNodeAttributeProvider nodeAttributeProvider,
                          AUGEdgeAttributeProvider edgeAttributeProvider) {
        exporter = new DOTExporter<>(nodeIdProvider,
                nodeLabelProvider, Edge::getLabel,
                nodeAttributeProvider, edgeAttributeProvider);
    }

    public String toDotGraph(APIUsageGraph aug) {
        return toDotGraph(aug, new HashMap<>());
    }

    public String toDotGraph(APIUsageGraph aug, Map<String, String> graphAttributes) {
        StringWriter writer = new StringWriter();
        toDotGraph(aug, graphAttributes, writer);
        return writer.toString();
    }

    private void toDotGraph(APIUsageGraph aug, Map<String, String> graphAttributes, Writer writer) {
        nodeIdProvider.clear();
        exporter.export(new PrintWriter(writer) {
            @Override
            public void write(String s, int off, int len) {
                if (s.equals("digraph G {")) {
                    String methodName = aug instanceof APIUsageExample ? ((APIUsageExample) aug).getLocation().getMethodSignature() : "AUG";
                    StringBuilder data = new StringBuilder("digraph \"").append(methodName).append("\" {").append(NEW_LINE);
                    for (Map.Entry<String, String> attribute : graphAttributes.entrySet()) {
                        data.append(attribute.getKey()).append("=").append(attribute.getValue()).append(";").append(NEW_LINE);
                    }
                    super.write(data.toString(), 0, data.length());
                } else {
                    super.write(s, off, len);
                }
            }
        }, aug);
    }

    public void toDotFile(APIUsageGraph aug, File file) throws IOException {
        if (!file.getPath().endsWith(".dot")) {
            file = new File(file.getPath() + ".dot");
        }
        file = file.getAbsoluteFile();
        ensureDirectory(file.getParentFile());
        try (BufferedWriter fout = new BufferedWriter(new FileWriter(file))) {
            fout.append(toDotGraph(aug));
            fout.flush();
        }
    }

    public void toPNGFile(APIUsageGraph aug, File file) throws IOException, InterruptedException {
        file = file.getAbsoluteFile();
        File directory = file.getParentFile();
        ensureDirectory(directory);
        String nameWithoutExtension = new File(directory, Files.getNameWithoutExtension(file.getPath())).getPath();
        toDotFile(aug, new File(nameWithoutExtension + ".dot"));
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(new String[]{EXEC_DOT, "-T"+ "png", nameWithoutExtension +".dot", "-o", nameWithoutExtension +"."+ "png"});
        p.waitFor();
    }

    private void ensureDirectory(File path) {
        if (!path.exists()) path.mkdirs();
    }
}