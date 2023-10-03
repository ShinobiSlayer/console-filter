package io.jenkins.plugins.console;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegexNameTest {

    public static final String REGEX = "/.*/console(Full)?$";
    public static final String NAME = "name";
    public static final String DONKEY = "donkey";
    private RegexName regexName;

    @BeforeEach
    void setUp() {
        regexName = new RegexName(NAME, REGEX, true);
    }

    @Test
    void isRegexEnabled() {
        assertTrue(regexName.isRegexEnabled());
    }

    @Test
    void setRegexEnabled() {
        regexName.setRegexEnabled(false);
        assertFalse(regexName.isRegexEnabled());
    }

    @Test
    void getRegex() {
        assertEquals(REGEX, regexName.getRegex());
    }

    @Test
    void setRegex() {
        regexName.setRegex(DONKEY);
        assertEquals(DONKEY, regexName.getRegex());
    }

    @Test
    void getName() {
        assertEquals(NAME, regexName.getName());
    }

    @Test
    void setName() {
        regexName.setName(DONKEY);
        assertEquals(DONKEY, regexName.getName());
    }

    @Test
    void getCompiledRegex() {
        assertEquals(REGEX, regexName.getCompiledRegex().pattern());
    }

    @Test
    void readResolve() {
        Object result = regexName.readResolve();
        assertEquals(regexName, result);
    }
}
