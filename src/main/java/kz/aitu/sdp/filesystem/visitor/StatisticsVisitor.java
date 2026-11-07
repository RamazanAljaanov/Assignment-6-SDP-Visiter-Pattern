package kz.aitu.sdp.filesystem.visitor;

import kz.aitu.sdp.filesystem.model.DirectoryNode;
import kz.aitu.sdp.filesystem.model.FileNode;
import kz.aitu.sdp.filesystem.model.FileSystemElement;

public class StatisticsVisitor implements FileSystemVisitor {

    private int fileCount = 0;
    private int directoryCount = 0;
    private int maxDepth = 0;
    private int currentDepth = 0;

    public void reset() {
        this.fileCount = 0;
        this.directoryCount = 0;
        this.maxDepth = 0;
        this.currentDepth = 0;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    public void visit(FileNode file) {
        fileCount++;
        maxDepth = Math.max(maxDepth, currentDepth);
    }

    @Override
    public void visit(DirectoryNode directory) {
        directoryCount++;
        maxDepth = Math.max(maxDepth, currentDepth);

        currentDepth++;
        for (FileSystemElement child : directory.getChildren()) {
            child.accept(this);
        }
        currentDepth--;
    }
}
