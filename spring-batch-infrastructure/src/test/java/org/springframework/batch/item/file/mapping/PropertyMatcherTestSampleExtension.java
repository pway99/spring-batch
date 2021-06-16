package org.springframework.batch.item.file.mapping;

/**
 * simple abstract class for testing PropertyMatcher
 */
public abstract class PropertyMatcherTestSampleExtension {
    private String name;
    public String getName(){
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
