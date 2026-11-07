package kz.aitu.sdp.filesystem.visitor;

import kz.aitu.sdp.filesystem.model.DirectoryNode;
import kz.aitu.sdp.filesystem.model.FileNode;
import kz.aitu.sdp.filesystem.model.FileSystemElement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class SearchVisitor implements FileSystemVisitor {

    private final Predicate<String> nameCriterion;
    private final long minSize;
    private final List<String> matchingPaths = new ArrayList<>();
    private final Deque<String> currentPathSegments = new ArrayDeque<>();
    private boolean atRoot = true;

    public SearchVisitor(String nameSubstring, long minSize) {
        this(name -> name != null && name.contains(Objects.requireNonNull(nameSubstring, "Search criterion cannot be null")), minSize);
    }

    public SearchVisitor(Predicate<String> nameCriterion, long minSize) {
        if (minSize < 0) {
            throw new IllegalArgumentException("Minimum size threshold cannot be negative: " + minSize);
        }
        this.nameCriterion = Objects.requireNonNull(nameCriterion, "Name criterion predicate cannot be null");
        this.minSize = minSize;
    }

    public void reset() {
        matchingPaths.clear();
        currentPathSegments.clear();
        atRoot = true;
    }

    public List<String> getMatchingPaths() {
        List<String> sorted = new ArrayList<>(matchingPaths);
        Collections.sort(sorted);
        return Collections.unmodifiableList(sorted);
    }

    @Override
    public void visit(FileNode file) {
        boolean nameMatch = nameCriterion.test(file.getName());
        boolean sizeMatch = file.getSize() >= minSize;

        if (nameMatch && sizeMatch) {
            matchingPaths.add(buildCurrentPath(file.getName()));
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        boolean isCurrentRoot = atRoot;
        if (isCurrentRoot) {
            atRoot = false;
        } else {
            currentPathSegments.addLast(directory.getName());
        }

        for (FileSystemElement child : directory.getChildren()) {
            child.accept(this);
        }

        if (!isCurrentRoot) {
            currentPathSegments.removeLast();
        }
    }

    private String buildCurrentPath(String fileName) {
        if (currentPathSegments.isEmpty()) {
            return fileName;
        }
        return String.join("/", currentPathSegments) + "/" + fileName;
    }
}
