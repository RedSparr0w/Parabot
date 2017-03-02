package org.parabot.environment.servers.executers;

import com.google.inject.Inject;
import org.parabot.api.io.Directories;
import org.parabot.api.io.WebUtil;
import org.parabot.api.io.build.BuildPath;
import org.parabot.core.Core;
import org.parabot.core.bdn.api.APIConfiguration;
import org.parabot.core.classpath.ClassPath;
import org.parabot.core.desc.ServerDescription;
import org.parabot.core.settings.Configuration;
import org.parabot.core.ui.components.VerboseLoader;
import org.parabot.core.ui.newui.components.DialogHelper;
import org.parabot.core.user.SharedUserAuthenticator;
import org.parabot.environment.api.utils.StringUtils;
import org.parabot.environment.servers.ServerProvider;
import org.parabot.environment.servers.loader.ServerLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;

/**
 * Fetches a server provider from the Parabot BDN
 *
 * @author Everel
 */
public class PublicServerExecutor extends ServerExecutor {

    private ServerDescription description;

    @Inject
    private SharedUserAuthenticator userAuthenticator;

    public PublicServerExecutor(final ServerDescription description) {
        this.description = description;
    }

    @Override
    public void run() {
        try {
            String cachedServerProviderName = StringUtils.toMD5(description.getDetail("provider"));

            final File destination = new File(Directories.getCachePath(),
                    cachedServerProviderName + ".jar");
            final String jarUrl = String.format(APIConfiguration.DOWNLOAD_SERVER_PROVIDER, Configuration.BOT_VERSION.isNightly());

            Core.verbose("Downloading provider...");

            if (destination.exists()) {
                Core.verbose("Found cached server provider [MD5: " + cachedServerProviderName + "]");
            } else {
                WebUtil.downloadFile(new URL(jarUrl), destination,
                        Core.getInjector().getInstance(VerboseLoader.class));
                Core.verbose("Server provider downloaded...");
            }

            final ClassPath classPath = new ClassPath();
            classPath.addJar(destination);

            BuildPath.add(destination.toURI().toURL());

            ServerLoader   serverLoader = new ServerLoader(classPath);
            final String[] classNames   = serverLoader.getServerClassNames();
            if (classNames.length == 0) {
                DialogHelper.showError(
                        "Parabot",
                        "Error loading provider",
                        "Failed to load server provider, error: [No provider found in jar file.]");
                return;
            } else if (classNames.length > 1) {
                DialogHelper.showError(
                        "Parabot",
                        "Error loading provider",
                        "Failed to load server provider, error: [Multiple providers found in jar file.]");
                return;
            }

            final String className = classNames[0];
            try {
                final Class<?> providerClass = serverLoader
                        .loadClass(className);
                final Constructor<?> con = providerClass.getConstructor();
                final ServerProvider serverProvider = (ServerProvider) con
                        .newInstance();
                serverProvider.setServerDescription(description);
                super.finalize(serverProvider);
            } catch (NoClassDefFoundError | ClassNotFoundException ignored) {
                DialogHelper.showError(
                        "Parabot",
                        "Error loading provider",
                        "Failed to load server provider, error: [This server provider is not compitable with this version of parabot]");
            } catch (Throwable t) {
                t.printStackTrace();
                DialogHelper.showError(
                        "Parabot",
                        "Error loading provider",
                        "Failed to load server provider, post the stacktrace/error on the parabot forums.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showError(
                    "Parabot",
                    "Error loading provider",
                    "Failed to load server provider, post the stacktrace/error on the parabot forums.");
        }

    }
}
