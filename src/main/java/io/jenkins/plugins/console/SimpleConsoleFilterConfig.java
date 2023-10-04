package io.jenkins.plugins.console;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Extension
public class SimpleConsoleFilterConfig extends GlobalConfiguration implements Serializable {

    private static final long serialVersionUID = 5850114662289551496L;

    private boolean enabled;

    private List<RegexName> regexes = new ArrayList<>();

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

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject formData) {
        setRegexes(req.bindJSONToList(RegexName.class, formData.get("regexes")));
        setEnabled(formData.getBoolean("enabled"));
        return true;
    }

    public static SimpleConsoleFilterConfig get(){
        return GlobalConfiguration.all().get(SimpleConsoleFilterConfig.class);
    }
}
