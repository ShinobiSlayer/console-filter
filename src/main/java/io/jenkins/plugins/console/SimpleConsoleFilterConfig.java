package io.jenkins.plugins.console;

import hudson.Extension;
import hudson.util.PersistedList;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * This class deals with the plugin configuration and persistence of the data.
 *
 * @author ccapdevi
 *
 */
@Extension
public class SimpleConsoleFilterConfig extends GlobalConfiguration implements Serializable {

    private static final long serialVersionUID = 5850114662289551496L;

    private static final Logger LOGGER = Logger.getLogger(SimpleConsoleFilterConfig.class.getName());

    private boolean enabled;

    private List<RegexName> regexes = new PersistedList<>(this);

    public SimpleConsoleFilterConfig() {
        load();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @DataBoundSetter
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public List<RegexName> getRegexes() {
        return regexes;
    }

    @DataBoundSetter
    public void setRegexes(List<RegexName> regexes) {
        this.regexes = regexes;
        save();
    }

    public static SimpleConsoleFilterConfig get() {
        final SimpleConsoleFilterConfig config;
        try {
            config = GlobalConfiguration.all().get(SimpleConsoleFilterConfig.class);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Config not found! " + e);
            throw e;
        }
        LOGGER.log(Level.INFO, "Found config.");
        return config;
    }
}
