/*
 * Moodle-Tools
 * Copyright (C) 2021  Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Affero General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along 
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package de.mnl.moodle.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing code taken from 
 * https://github.com/jenkinsci/git-client-plugin/blob/master/src/main/java/org/jenkinsci/plugins/gitclient/NetrcAuthenticator.java
 * and adapted.
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
class NetrcAuthenticator extends Authenticator {
    private static final Pattern NETRC_TOKEN = Pattern.compile("(\\S+)");

    /** Parsing states */
    private enum ParseState {
        START, REQ_KEY, REQ_VALUE, MACHINE, LOGIN, PASSWORD, MACDEF, END
    }

    private File netrc;
    private long lastModified;
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final Map<String, PasswordAuthentication> hosts = new HashMap<>();

    /**
     * Create an instance that looks for the ".netrc" (or "_netrc", Windows)
     * in the current working directory and the home directory.
     */
    public NetrcAuthenticator() {
        this(getDefaultFile());
    }

    private static File getDefaultFile() {
        File home = new File(System.getProperty("user.home"));
        File netrc = new File(home, ".netrc");
        if (!netrc.exists()) {
            netrc = new File(home, "_netrc"); // windows variant
        }
        return netrc;
    }

    /**
     * Create an instance with the information from the given file.
     *
     * @param netrc the netrc
     */
    public NetrcAuthenticator(File netrc) {
        this.netrc = netrc;
        if (!netrc.exists()) {
            throw new IllegalArgumentException("File .netrc not found.");
        }
        parse();
    }

    @SuppressWarnings({ "PMD.AvoidSynchronizedAtMethodLevel",
        "PMD.CognitiveComplexity", "PMD.CyclomaticComplexity",
        "PMD.ExcessiveMethodLength", "PMD.NcssCount", "PMD.AssignmentInOperand",
        "PMD.AvoidInstantiatingObjectsInLoops" })
    private synchronized void parse() {
        hosts.clear();
        lastModified = netrc.lastModified();

        try (BufferedReader netrcContent = new BufferedReader(
            new InputStreamReader(Files.newInputStream(netrc.toPath())))) {
            String line;
            String machine = null;
            String login = null;
            String password = null;

            ParseState state = ParseState.START;
            Matcher matcher = NETRC_TOKEN.matcher("");
            while ((line = netrcContent.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (state == ParseState.MACDEF) {
                        state = ParseState.REQ_KEY;
                    }
                    continue;
                }

                matcher.reset(line);
                while (matcher.find()) {
                    String match = matcher.group();
                    switch (state) {
                    case START:
                        if ("machine".equals(match)) {
                            state = ParseState.MACHINE;
                        }
                        break;

                    case REQ_KEY:
                        if (null == match) {
                            state = ParseState.REQ_VALUE;
                        } else {
                            switch (match) {
                            case "login":
                                state = ParseState.LOGIN;
                                break;
                            case "password":
                                state = ParseState.PASSWORD;
                                break;
                            case "macdef":
                                state = ParseState.MACDEF;
                                break;
                            case "machine":
                                state = ParseState.MACHINE;
                                break;
                            default:
                                state = ParseState.REQ_VALUE;
                                break;
                            }
                        }
                        break;

                    case REQ_VALUE:
                        state = ParseState.REQ_KEY;
                        break;

                    case MACHINE:
                        if (machine != null && login != null
                            && password != null) {
                            hosts.put(machine,
                                new PasswordAuthentication(login,
                                    password.toCharArray()));
                        }
                        machine = match;
                        login = null;
                        password = null;
                        state = ParseState.REQ_KEY;
                        break;

                    case LOGIN:
                        login = match;
                        state = ParseState.REQ_KEY;
                        break;

                    case PASSWORD:
                        password = match;
                        state = ParseState.REQ_KEY;
                        break;

                    case MACDEF:
                        // Only way out is an empty line, handled before the
                        // find() loop.
                        break;

                    default:
                        break;
                    }
                }
            }
            if (machine != null && login != null && password != null) {
                hosts.put(machine, new PasswordAuthentication(login,
                    password.toCharArray()));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                "Invalid netrc file: '" + netrc.getAbsolutePath() + "'", e);
        }
    }

    @Override
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public PasswordAuthentication getPasswordAuthentication() {
        if (lastModified != netrc.lastModified()) {
            parse();
        }
        String machine = getRequestingHost() == null
            ? getRequestingSite().getHostAddress()
            : getRequestingHost();
        if (getRequestingProtocol() != null) {
            machine = getRequestingProtocol() + "://" + machine;
        }
        if (getRequestingPort() != 0) {
            machine += ":" + getRequestingPort();
        }
        return hosts.get(machine);
    }

}
