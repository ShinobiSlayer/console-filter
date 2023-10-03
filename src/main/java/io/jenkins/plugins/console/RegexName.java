package io.jenkins.plugins.console;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serializable;
import java.util.regex.Pattern;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class RegexName extends AbstractDescribableImpl<RegexName> implements Serializable {

    private static final long serialVersionUID = -5445325528650992703L;
    private String name;
    private String regex;
    private boolean regexEnabled;
    private transient Pattern compiledRegex;

    @DataBoundConstructor
    public RegexName(String name, String regex, boolean regexEnabled) {
        this.name = name;
        this.regex = regex;
        this.regexEnabled = regexEnabled;
        this.compiledRegex = Pattern.compile(this.regex);
    }

    public boolean isRegexEnabled() {
        return regexEnabled;
    }

    @DataBoundSetter
    public void setRegexEnabled(boolean regexEnabled) {
        this.regexEnabled = regexEnabled;
    }

    public String getRegex() {
        return regex;
    }

    @DataBoundSetter
    public void setRegex(String regex) {
        this.regex = regex;
        this.compiledRegex = Pattern.compile(this.regex);
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public Pattern getCompiledRegex() {
        return compiledRegex;
    }

    @SuppressWarnings("unused")
    protected Object readResolve() {
        this.compiledRegex = Pattern.compile(this.regex);
        return this;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RegexName> {
        public String getDisplayName() {
            return "Name and regular expression.";
        }
    }
}
