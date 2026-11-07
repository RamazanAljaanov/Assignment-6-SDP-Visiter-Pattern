package kz.aitu.sdp.filesystem;

import kz.aitu.sdp.filesystem.model.DirectoryNode;
import kz.aitu.sdp.filesystem.model.FileNode;
import kz.aitu.sdp.filesystem.visitor.SearchVisitor;
import kz.aitu.sdp.filesystem.visitor.StatisticsVisitor;
import kz.aitu.sdp.filesystem.visitor.TotalSizeVisitor;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println(" ASTANA IT UNIVERSITY | ShP-2216 Software Design Patterns | Assignment 06");
        System.out.println(" Topic: Advanced File System with Visitor Pattern & Double Dispatch");
        System.out.println("================================================================================\n");

        System.out.println("[1] Constructing hierarchical file tree...");
        DirectoryNode root = new DirectoryNode("sdp-project");

        FileNode readme = new FileNode("README.md", 1240);
        FileNode pom = new FileNode("pom.xml", 2450);
        FileNode gitignore = new FileNode(".gitignore", 310);
        root.addChild(readme);
        root.addChild(pom);
        root.addChild(gitignore);

        DirectoryNode src = new DirectoryNode("src");
        DirectoryNode mainDir = new DirectoryNode("main");
        DirectoryNode javaMain = new DirectoryNode("java");
        javaMain.addChild(new FileNode("Application.java", 4800));
        javaMain.addChild(new FileNode("Service.java", 3100));
        mainDir.addChild(javaMain);

        DirectoryNode resources = new DirectoryNode("resources");
        resources.addChild(new FileNode("application.properties", 420));
        mainDir.addChild(resources);
        src.addChild(mainDir);

        DirectoryNode testDir = new DirectoryNode("test");
        DirectoryNode javaTest = new DirectoryNode("java");
        javaTest.addChild(new FileNode("ApplicationTest.java", 2800));
        testDir.addChild(javaTest);
        src.addChild(testDir);

        root.addChild(src);

        DirectoryNode docs = new DirectoryNode("docs");
        docs.addChild(new FileNode("architecture.png", 15200));
        docs.addChild(new FileNode("specification.pdf", 8500));
        root.addChild(docs);

        DirectoryNode tempLogs = new DirectoryNode("logs");
        root.addChild(tempLogs);

        System.out.println("   Tree structure successfully created.\n");

        System.out.println("[2] Executing TotalSizeVisitor (R3):");
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        long totalBytes = sizeVisitor.getTotalSize();
        System.out.printf("   -> Total size of entire subtree: %d bytes (%.2f KB)%n\n",
                totalBytes, totalBytes / 1024.0);

        System.out.println("[3] Executing SearchVisitor (R4):");
        System.out.println("   Criterion: file name contains '.java' AND size >= 3000 bytes");
        SearchVisitor searchVisitor = new SearchVisitor(".java", 3000);
        root.accept(searchVisitor);
        List<String> matchingPaths = searchVisitor.getMatchingPaths();
        System.out.println("   -> Deterministic root-relative results (lexicographically sorted):");
        if (matchingPaths.isEmpty()) {
            System.out.println("      (no matches found)");
        } else {
            for (String path : matchingPaths) {
                System.out.println("      * " + path);
            }
        }
        System.out.println();

        System.out.println("[4] Executing StatisticsVisitor (R5, R8 - Added via OCP):");
        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        System.out.println("   -> Collected File System Statistics:");
        System.out.println("      * Total files:       " + statsVisitor.getFileCount());
        System.out.println("      * Total directories: " + statsVisitor.getDirectoryCount() + " (including root)");
        System.out.println("      * Maximum depth:     " + statsVisitor.getMaxDepth() + " (root depth = 0)\n");

        System.out.println("[5] Demonstrating Tree Integrity Validations (R6):");
        demonstrateIntegrityChecks(root, docs);

        System.out.println("\n================================================================================");
        System.out.println(" All demonstrations completed successfully with 100% invariant preservation.");
        System.out.println("================================================================================");
    }

    private static void demonstrateIntegrityChecks(DirectoryNode root, DirectoryNode docs) {
        try {
            new FileNode("invalid.dat", -100);
        } catch (IllegalArgumentException ex) {
            System.out.println("   [OK] Negative size rejected: " + ex.getMessage());
        }

        try {
            root.addChild(new FileNode("README.md", 500));
        } catch (IllegalArgumentException ex) {
            System.out.println("   [OK] Duplicate sibling rejected: " + ex.getMessage());
        }

        try {
            docs.addChild(root);
        } catch (IllegalStateException ex) {
            System.out.println("   [OK] Cycle rejected: " + ex.getMessage());
        }
    }
}
