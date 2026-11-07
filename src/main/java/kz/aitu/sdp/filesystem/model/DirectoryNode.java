package kz.aitu.sdp.filesystem.model;

import kz.aitu.sdp.filesystem.visitor.FileSystemVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DirectoryNode implements FileSystemElement {

    private final String name;
    private final List<FileSystemElement> children = new ArrayList<>();
    private DirectoryNode parent;

    public DirectoryNode(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Directory name cannot be null or blank");
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DirectoryNode getParent() {
        return parent;
    }

    @Override
    public void setParent(DirectoryNode parent) {
        this.parent = parent;
    }

    public void addChild(FileSystemElement child) {
        Objects.requireNonNull(child, "Child element cannot be null");

        DirectoryNode currentAncestor = this;
        while (currentAncestor != null) {
            if (currentAncestor == child) {
                throw new IllegalStateException("Cycle detected: cannot add ancestor directory '"
                        + child.getName() + "' as a child of '" + this.name + "'");
            }
            currentAncestor = currentAncestor.getParent();
        }

        if (child.getParent() != null && child.getParent() != this) {
            throw new IllegalStateException("Element '" + child.getName()
                    + "' already belongs to directory '" + child.getParent().getName() + "'");
        }

        for (FileSystemElement existing : children) {
            if (existing.getName().equals(child.getName())) {
                throw new IllegalArgumentException("Duplicate sibling name detected: '" + child.getName()
                        + "' already exists in directory '" + this.name + "'");
            }
        }

        children.add(child);
        child.setParent(this);
    }

    public boolean removeChild(FileSystemElement child) {
        if (child != null && children.remove(child)) {
            child.setParent(null);
            return true;
        }
        return false;
    }

    public List<FileSystemElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public int getChildCount() {
        return children.size();
    }

    @Override
    public void accept(FileSystemVisitor visitor) {
        Objects.requireNonNull(visitor, "Visitor cannot be null");
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "DirectoryNode[name='" + name + "', children=" + children.size() + "]";
    }
}
