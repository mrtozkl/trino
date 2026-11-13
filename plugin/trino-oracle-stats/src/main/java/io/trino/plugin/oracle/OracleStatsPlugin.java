/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.oracle;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.airlift.bootstrap.Bootstrap;
import io.trino.plugin.base.ConnectorContextModule;
import io.trino.plugin.jdbc.ExtraCredentialsBasedIdentityCacheMappingModule;
import io.trino.plugin.jdbc.JdbcConnector;
import io.trino.plugin.jdbc.JdbcModule;
import io.trino.plugin.jdbc.credential.CredentialProviderModule;
import io.trino.spi.Plugin;
import io.trino.spi.connector.Connector;
import io.trino.spi.connector.ConnectorContext;
import io.trino.spi.connector.ConnectorFactory;

import java.util.Map;

import static io.airlift.configuration.ConfigurationAwareModule.combine;
import static java.util.Objects.requireNonNull;

public class OracleStatsPlugin
        implements Plugin
{
    @Override
    public Iterable<ConnectorFactory> getConnectorFactories()
    {
        return ImmutableList.of(new ConnectorFactory()
        {
            @Override
            public String getName()
            {
                return "oracle_stats";
            }

            @Override
            public Connector create(String catalogName, Map<String, String> requiredConfig, ConnectorContext context)
            {
                requireNonNull(requiredConfig, "requiredConfig is null");

                Bootstrap app = new Bootstrap(
                        "io.trino.bootstrap.catalog." + catalogName,
                        new ConnectorContextModule(catalogName, context),
                        new JdbcModule(),
                        combine(
                                new CredentialProviderModule(),
                                new ExtraCredentialsBasedIdentityCacheMappingModule(),
                                new OracleClientModule()));

                Injector injector = app
                        .doNotInitializeLogging()
                        .disableSystemProperties()
                        .setRequiredConfigurationProperties(requiredConfig)
                        .initialize();

                return injector.getInstance(JdbcConnector.class);
            }
        });
    }
}
