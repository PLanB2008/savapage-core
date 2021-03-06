/*
 * This file is part of the SavaPage project <https://www.savapage.org>.
 * Copyright (c) 2011-2020 Datraverse B.V.
 * Author: Rijk Ravestein.
 *
 * SPDX-FileCopyrightText: 2011-2020 Datraverse B.V. <info@datraverse.com>
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For more information, please contact Datraverse B.V. at this
 * address: info@datraverse.com
 */
package org.savapage.core.cli.server;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.savapage.core.cli.AbstractApp;
import org.savapage.core.config.ConfigManager;
import org.savapage.core.util.InetUtils;

/**
 * <p>
 * Main CLI entry point for PUBLIC server functions via JSON-RPC.
 * </p>
 * <p>
 * It accepts the commands as arguments and outputs the results of the command
 * on the console (standard-out). For security reasons only users with read
 * access to the server.properties (normally only the Administrators group) have
 * rights to execute the commands.
 * </p>
 *
 * @author Rijk Ravestein
 *
 */
public final class App extends AbstractApp {

    /**
     *
     */
    protected static final String CLI_SWITCH_HELP_ALL_LONG = "help-all";

    /**
     *
     */
    private final List<String> optionNames = new ArrayList<>();

    /**
     *
     */
    private App() {
    }

    /**
     * Creates the method option for this command.
     *
     * @param options
     *            The {@link Options} to add the option to.
     * @param optionNames
     *            The list to add the CLI method name to.
     * @param appApi
     *            The CLI method.
     */
    public static void addMethodOption(final Options options,
            final List<String> optionNames, final AbstractAppApi appApi) {

        final Option.Builder optionBuilder = Option.builder();

        optionBuilder.hasArg(false);
        optionBuilder.longOpt(appApi.getCliMethodName());
        optionBuilder.desc(appApi.getShortDecription());

        options.addOption(optionBuilder.build());

        optionNames.add(appApi.getCliMethodName());
    }

    /**
     *
     */
    private static final AbstractAppApi[] APP_API_LIST = new AbstractAppApi[] {
            //
            new CliAddInternalUser(), //
            new CliAddUserGroup(), //
            new CliChangeBaseCurrency(), //
            new CliDeleteUser(), //
            new CliDeleteUserGroup(), //
            new CliDeleteUserGroupAccount(), //
            new CliEraseUser(), //
            new CliGetConfigProperty(), //
            new CliListUsers(), //
            new CliListUserGroups(), //
            new CliListUserGroupMembers(), //
            new CliListUserGroupMemberships(), //
            new CliListUserSourceGroups(), //
            new CliListUserSourceGroupMembers(), //
            new CliListUserSourceGroupNesting(), //
            new CliPrinterAccessControl(), //
            new CliPrinterSnmp(), //
            new CliSetConfigProperty(), //
            new CliSetUserProperties(), //
            new CliSetUserGroupProperties(), //
            new CliSyncUserGroup(), //
            new CliSyncUsersAndGroups(), //
            new CliSystemStatus() //
    };

    @Override
    protected Options createCliOptions() throws Exception {

        Options options = new Options();

        for (AbstractAppApi appApi : APP_API_LIST) {
            addMethodOption(options, optionNames, appApi);
        }

        //
        String opt;
        //
        opt = CLI_SWITCH_HELP_LONG;
        optionNames.add(opt);
        options.addOption(opt, CLI_SWITCH_HELP_LONG, false,
                "Displays this help text.");
        //
        opt = CLI_SWITCH_HELP_ALL_LONG;
        optionNames.add(opt);
        options.addOption(null, CLI_SWITCH_HELP_ALL_LONG, false,
                "Displays help text of all methods.");

        //
        return options;
    }

    /**
     * @throws Exception
     *             When CLI errors.
     */
    private void allUsages() throws Exception {

        final String[] helpArg = new String[] { "--" + CLI_SWITCH_HELP_LONG };

        for (AbstractAppApi appApi : APP_API_LIST) {
            appApi.runApi(helpArg);
        }
    }

    /**
     *
     * @param cmd
     *            The {@link CommandLine}.
     * @return {@code null} when not found.
     */
    private AbstractAppApi getAppApi(final CommandLine cmd) {

        for (AbstractAppApi appApi : APP_API_LIST) {
            if (cmd.hasOption(appApi.getCliMethodName())) {
                return appApi;
            }
        }
        return null;
    }

    /**
     * @return The server host address description.
     */
    private String getApiDescriptServerAddress() {
        final StringBuilder str = new StringBuilder("Server host address: ");
        try {
            str.append(InetUtils.getServerHostAddress());
        } catch (UnknownHostException e) {
            str.append("unknown");
        }
        return str.append(System.lineSeparator()).toString();
    }

    @Override
    public int run(final String[] args) throws Exception {

        final String cmdLineSyntax = "[METHOD] [OPTION]...";

        final String descript = AbstractAppApi.getApiDescriptHeader()
                + getApiDescriptServerAddress() + System.lineSeparator()
                + "Note: use METHOD --help for method details.";

        // ......................................................
        // Parse FIRST parameter from CLI
        // ......................................................
        String[] argsFirst = null;
        if (args.length > 0) {
            argsFirst = Arrays.copyOfRange(args, 0, 1);
        }

        Options options = createCliOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, argsFirst);
        } catch (org.apache.commons.cli.ParseException e) {
            getDisplayStream().println(e.getMessage());
            usage(cmdLineSyntax, descript, options, optionNames);
            return EXIT_CODE_PARMS_PARSE_ERROR;
        }

        // ......................................................
        // Help needed?
        // ......................................................
        if (cmd.hasOption(CLI_SWITCH_HELP_ALL_LONG)) {
            allUsages();
            return EXIT_CODE_OK;
        } else if (cmd.hasOption(CLI_SWITCH_HELP)
                || cmd.hasOption(CLI_SWITCH_HELP_LONG)) {
            if (args.length == 1) {
                usage(cmdLineSyntax, descript, options, optionNames);
            } else {
                AbstractAppApi appApi = getAppApi(cmd);
                if (appApi != null) {
                    appApi.runApi(null);
                } else {
                    usage(cmdLineSyntax, descript, options, optionNames);
                }
            }
            return EXIT_CODE_OK;
        }

        // ...................................................................
        // Hardcoded default parameter values
        // ...................................................................

        /*
         * Initialize this application.
         */
        init();

        int ret = EXIT_CODE_EXCEPTION;

        try {
            AbstractAppApi appApi = getAppApi(cmd);

            if (appApi != null) {
                ret = appApi.runApi(Arrays.copyOfRange(args, 1, args.length));
            } else {
                usage(cmdLineSyntax, descript, options, optionNames);
                ret = EXIT_CODE_MISSING_PARMS;
            }

        } catch (Exception ex) {

            getErrorDisplayStream().println(
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());

        } finally {
            ConfigManager.instance().exit();
        }

        return ret;
    }

    @Override
    protected void onInit() throws Exception {
    }

    /**
     * IMPORTANT: MUST return void, use System.exit() to get an exit code on JVM
     * execution.
     *
     * @param args
     *            The CLI arguments
     */
    public static void main(final String[] args) {

        int status = EXIT_CODE_EXCEPTION;

        final App app = new App();

        try {
            status = app.run(args);
        } catch (Exception e) {
            getErrorDisplayStream().println(e.getMessage());
        }

        System.exit(status);
    }
}
