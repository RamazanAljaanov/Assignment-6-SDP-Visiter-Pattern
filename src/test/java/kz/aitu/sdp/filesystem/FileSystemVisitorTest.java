package kz.aitu.sdp.filesystem;

import kz.aitu.sdp.filesystem.model.DirectoryNode;
import kz.aitu.sdp.filesystem.model.FileNode;
import kz.aitu.sdp.filesystem.model.FileSystemElement;
import kz.aitu.sdp.filesystem.visitor.FileSystemVisitor;
import kz.aitu.sdp.filesystem.visitor.SearchVisitor;
import kz.aitu.sdp.filesystem.visitor.StatisticsVisitor;
import kz.aitu.sdp.filesystem.visitor.TotalSizeVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemVisitorTest {

    @Test
    @DisplayName("T1: Both element types reach the correct overloaded visit methods via double dispatch")
    void testT1_doubleDispatchRouting() {
        FileNode file = new FileNode("test.txt", 100);
        DirectoryNode directory = new DirectoryNode("testDir");

        AtomicBoolean fileVisited = new AtomicBoolean(false);
        AtomicBoolean dirVisited = new AtomicBoolean(false);

        FileSystemVisitor testVisitor = new FileSystemVisitor() {
            @Override
            public void visit(FileNode f) {
                assertSame(file, f);
                fileVisited.set(true);
            }

            @Override
            public void visit(DirectoryNode d) {
                assertSame(directory, d);
                dirVisited.set(true);
            }
        };

        file.accept(testVisitor);
        assertTrue(fileVisited.get());
        assertFalse(dirVisited.get());

        fileVisited.set(false);
        directory.accept(testVisitor);
        assertTrue(dirVisited.get());
        assertFalse(fileVisited.get());
    }

    @Test
    @DisplayName("T2: Size accumulation across multiple nesting levels is correct")
    void testT2_sizeAccumulationAcrossNestingLevels() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode rootFile = new FileNode("root.log", 200);
        root.addChild(rootFile);

        DirectoryNode sub1 = new DirectoryNode("sub1");
        FileNode sub1File = new FileNode("data.bin", 500);
        sub1.addChild(sub1File);
        root.addChild(sub1);

        DirectoryNode sub2 = new DirectoryNode("sub2");
        FileNode deepFile = new FileNode("archive.zip", 1300);
        sub2.addChild(deepFile);
        sub1.addChild(sub2);

        DirectoryNode emptyDir = new DirectoryNode("empty");
        root.addChild(emptyDir);

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);

        assertEquals(2000L, sizeVisitor.getTotalSize());
    }

    @Test
    @DisplayName("T3: Search correctly combines both configurable filter conditions (name and min-size)")
    void testT3_searchCombinesBothFilters() {
        DirectoryNode root = new DirectoryNode("project");
        FileNode f1 = new FileNode("report_final.pdf", 600);
        FileNode f2 = new FileNode("report_draft.pdf", 100);
        FileNode f3 = new FileNode("notes.txt", 800);
        FileNode f4 = new FileNode("summary.pdf", 50);

        root.addChild(f1);
        root.addChild(f2);
        root.addChild(f3);
        root.addChild(f4);

        SearchVisitor searchVisitor = new SearchVisitor("report", 500);
        root.accept(searchVisitor);

        List<String> results = searchVisitor.getMatchingPaths();
        assertEquals(1, results.size());
        assertEquals("report_final.pdf", results.get(0));
    }

    @Test
    @DisplayName("T4: Search produces correct, deterministic, root-relative paths sorted lexicographically")
    void testT4_searchProducesDeterministicRootRelativePaths() {
        DirectoryNode root = new DirectoryNode("workspace");

        FileNode bFile = new FileNode("b_script.sh", 150);
        FileNode aFile = new FileNode("a_script.sh", 150);
        root.addChild(bFile);
        root.addChild(aFile);

        DirectoryNode srcDir = new DirectoryNode("src");
        FileNode mainJava = new FileNode("Main.java", 300);
        srcDir.addChild(mainJava);
        root.addChild(srcDir);

        DirectoryNode docsDir = new DirectoryNode("docs");
        DirectoryNode archDir = new DirectoryNode("arch");
        FileNode diagram = new FileNode("diagram.png", 500);
        archDir.addChild(diagram);
        docsDir.addChild(archDir);
        root.addChild(docsDir);

        SearchVisitor searchVisitor = new SearchVisitor("", 100);
        root.accept(searchVisitor);

        List<String> paths = searchVisitor.getMatchingPaths();

        List<String> expected = List.of(
                "a_script.sh",
                "b_script.sh",
                "docs/arch/diagram.png",
                "src/Main.java"
        );

        assertEquals(expected, paths);
    }

    @Test
    @DisplayName("T5: Statistics are correct for nesting, counting, and maximum depth")
    void testT5_statisticsVisitorAccuracy() {
        DirectoryNode root = new DirectoryNode("root");

        FileNode fileA = new FileNode("fileA.txt", 10);
        DirectoryNode dir1 = new DirectoryNode("dir1");
        root.addChild(fileA);
        root.addChild(dir1);

        DirectoryNode dir2 = new DirectoryNode("dir2");
        dir1.addChild(dir2);

        FileNode deepFile = new FileNode("deep.txt", 20);
        dir2.addChild(deepFile);

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);

        assertEquals(3, statsVisitor.getDirectoryCount());
        assertEquals(2, statsVisitor.getFileCount());
        assertEquals(3, statsVisitor.getMaxDepth());
    }

    @Test
    @DisplayName("T6: Empty structures and no-match search cases are handled properly")
    void testT6_emptyStructuresAndNoMatch() {
        DirectoryNode emptyRoot = new DirectoryNode("emptyRoot");

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        emptyRoot.accept(sizeVisitor);
        assertEquals(0L, sizeVisitor.getTotalSize());

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        emptyRoot.accept(statsVisitor);
        assertEquals(1, statsVisitor.getDirectoryCount());
        assertEquals(0, statsVisitor.getFileCount());
        assertEquals(0, statsVisitor.getMaxDepth());

        SearchVisitor searchVisitor = new SearchVisitor("nonexistent", 1000);
        emptyRoot.accept(searchVisitor);
        assertTrue(searchVisitor.getMatchingPaths().isEmpty());
    }

    @Test
    @DisplayName("T7: Repeat executions do not leak or multiply accumulated state")
    void testT7_repeatExecutionsDoNotLeakState() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("file1.txt", 100));
        root.addChild(new FileNode("file2.txt", 200));

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        assertEquals(300L, sizeVisitor.getTotalSize());

        sizeVisitor.reset();
        root.accept(sizeVisitor);
        assertEquals(300L, sizeVisitor.getTotalSize());

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        assertEquals(1, statsVisitor.getDirectoryCount());
        assertEquals(2, statsVisitor.getFileCount());
        assertEquals(1, statsVisitor.getMaxDepth());

        statsVisitor.reset();
        DirectoryNode emptyDir = new DirectoryNode("singleDir");
        emptyDir.accept(statsVisitor);
        assertEquals(1, statsVisitor.getDirectoryCount());
        assertEquals(0, statsVisitor.getFileCount());
        assertEquals(0, statsVisitor.getMaxDepth());

        SearchVisitor searchVisitor = new SearchVisitor("file1", 50);
        root.accept(searchVisitor);
        assertEquals(1, searchVisitor.getMatchingPaths().size());

        searchVisitor.reset();
        emptyDir.accept(searchVisitor);
        assertTrue(searchVisitor.getMatchingPaths().isEmpty());
    }

    @Test
    @DisplayName("T8: Invalid tree mutations are rejected while valid nesting remains supported")
    void testT8_treeIntegrityEnforcement() {
        IllegalArgumentException sizeEx = assertThrows(IllegalArgumentException.class,
                () -> new FileNode("bad.txt", -1));
        assertTrue(sizeEx.getMessage().contains("negative"));

        assertThrows(IllegalArgumentException.class, () -> new FileNode("", 10));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode("   "));

        DirectoryNode parent = new DirectoryNode("parent");
        parent.addChild(new FileNode("duplicate.txt", 100));
        IllegalArgumentException dupEx = assertThrows(IllegalArgumentException.class,
                () -> parent.addChild(new FileNode("duplicate.txt", 200)));
        assertTrue(dupEx.getMessage().contains("Duplicate"));

        DirectoryNode otherParent = new DirectoryNode("otherParent");
        FileNode sharedFile = new FileNode("shared.txt", 50);
        parent.addChild(sharedFile);

        IllegalStateException parentEx = assertThrows(IllegalStateException.class,
                () -> otherParent.addChild(sharedFile));
        assertTrue(parentEx.getMessage().contains("already belongs"));

        assertTrue(parent.removeChild(sharedFile));
        assertNull(sharedFile.getParent());
        assertDoesNotThrow(() -> otherParent.addChild(sharedFile));

        DirectoryNode a = new DirectoryNode("A");
        DirectoryNode b = new DirectoryNode("B");
        DirectoryNode c = new DirectoryNode("C");

        a.addChild(b);
        b.addChild(c);

        IllegalStateException cycleEx = assertThrows(IllegalStateException.class,
                () -> c.addChild(a));
        assertTrue(cycleEx.getMessage().contains("Cycle detected"));

        assertThrows(IllegalStateException.class, () -> a.addChild(a));

        List<FileSystemElement> children = a.getChildren();
        assertThrows(UnsupportedOperationException.class,
                () -> children.add(new FileNode("hacked.txt", 10)));
    }
}
