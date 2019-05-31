/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.provider;

import bisq.core.app.AppOptionKeys;
import bisq.core.app.BisqEnvironment;

import bisq.network.NetworkOptionKeys;

import com.google.inject.Inject;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Slf4j
public class ProvidersRepository {
    private static final List<String> DEFAULT_NODES = Arrays.asList(
            "18.179.40.172:8333",
            "18.208.61.185:8333",
            "31.220.56.195:8333",
            "35.220.226.25:8333",
            "35.227.56.27:8333",
            "37.48.83.207:8333",
            "38.87.54.163:8334",
            "38.143.66.14:8333",
            "47.89.180.57:8333",
            "47.93.174.61:8333",
            "47.98.224.31:28333",
            "47.147.194.165:8333",
            "47.254.42.16:8333",
            "50.225.198.67:6628",
            "52.243.44.176:8333",
            "52.246.166.192:8333",
            "54.95.31.146:8333",
            "54.210.151.7:8333",
            "60.191.106.150:8333",
            "60.249.215.221:8333",
            "62.42.138.162:8333",
            "63.143.32.126:8433",
            "66.96.199.249:8333",
            "67.239.3.146:8333",
           "73.76.218.50:8333",
            "78.97.206.149:8333",
            "81.237.206.224:8353",
            "82.75.64.15:8333",
            "82.200.205.30:8331",
            "83.172.69.154:8333",
            "84.112.174.5:8222",
            "88.99.48.7:9090",
            "89.179.247.236:8333",
            "91.148.141.242:8333",
            "92.206.113.127:8333",
            "94.199.178.17:8022",
            "94.247.134.76:8333",
            "95.79.35.133:7333",
            "95.172.230.70:8333",
            "100.1.209.114:8333",
            "100.11.124.171:8333",
            "104.238.131.116:8333",
            "107.175.46.159:8334",
            "107.191.117.175:8333",
            "109.70.144.105:8333",
            "109.134.181.120:8334",
            "111.90.145.37:8334",
            "113.10.152.126:8333",
            "128.199.138.39:8333",
            "141.239.183.148:8333",
            "142.68.56.141:8333",
            "144.76.102.2:8333",
            "146.90.44.252:8333",
            "148.70.154.102:8333",
            "158.69.84.33:8333",
            "162.213.252.3:8333",
            "162.242.168.36:8333",
            "162.242.168.55:8333",
            "163.172.142.149:10020",
            "172.96.161.245:8333",
            "172.249.77.148:8333",
            "173.82.103.250:8333",
            "173.212.202.187:8335",
            "173.214.244.102:8333",
            "173.224.240.45:8333",
            "178.128.118.37:8333",
            "179.218.80.242:8333",
            "188.134.90.224:8333",
            "188.214.30.3:8333",
            "188.241.58.206:8333",
            "190.2.140.16:8333",
            "193.169.244.189:8333",
            "194.14.246.205:8444",
            "194.14.247.116:8333",
            "195.122.150.173:8333",
            "195.154.168.129:8333",
            "198.27.68.86:8333",
            "198.204.229.34:8333",
            "204.44.118.195:8333",
            "212.107.44.171:10333",
            "213.165.68.218:8333",
            "213.227.140.194:8333",
            "216.218.235.93:8333"
    );

    private final String providersFromProgramArgs;
    private final boolean useLocalhostForP2P;

    private List<String> providerList;
    @Getter
    private String baseUrl = "";
    @Getter
    @Nullable
    private List<String> bannedNodes;
    private int index = 0;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public ProvidersRepository(BisqEnvironment bisqEnvironment,
                               @Named(AppOptionKeys.PROVIDERS) String providers,
                               @Named(NetworkOptionKeys.USE_LOCALHOST_FOR_P2P) boolean useLocalhostForP2P) {

        this.providersFromProgramArgs = providers;
        this.useLocalhostForP2P = useLocalhostForP2P;

        Collections.shuffle(DEFAULT_NODES);

        applyBannedNodes(bisqEnvironment.getBannedPriceRelayNodes());
    }

    public void applyBannedNodes(@Nullable List<String> bannedNodes) {
        this.bannedNodes = bannedNodes;
        fillProviderList();
        selectNextProviderBaseUrl();

        if (bannedNodes == null)
            log.info("Selected provider baseUrl={}, providerList={}", baseUrl, providerList);
        else
            log.warn("We have banned provider nodes: bannedNodes={}, selected provider baseUrl={}, providerList={}",
                    bannedNodes, baseUrl, providerList);
    }

    public void selectNextProviderBaseUrl() {
        if (!providerList.isEmpty()) {
            if (index >= providerList.size())
                index = 0;

            baseUrl = providerList.get(index);
            index++;

            if (providerList.size() == 1 && BisqEnvironment.getBaseCurrencyNetwork().isMainnet())
                log.warn("We only have one provider");
        } else {
            baseUrl = "";
            log.warn("We do not have any providers. That can be if all providers are filtered or providersFromProgramArgs is set but empty. " +
                    "bannedNodes={}. providersFromProgramArgs={}", bannedNodes, providersFromProgramArgs);
        }
    }

    private void fillProviderList() {
        List<String> providers;
        if (providersFromProgramArgs == null || providersFromProgramArgs.isEmpty()) {
            if (useLocalhostForP2P) {
                // If we run in localhost mode we don't have the tor node running, so we need a clearnet host
                // Use localhost for using a locally running provider
                // providerAsString = Collections.singletonList("http://localhost:8080/");
                //providers = Collections.singletonList("http://174.138.104.137:8080/"); // @miker
                //Testing localhost
                providers = Collections.singletonList("http://127.0.0.1/");
            } else {
                providers = DEFAULT_NODES;
            }
        } else {
            providers = Arrays.asList(StringUtils.deleteWhitespace(providersFromProgramArgs).split(","));
        }
        providerList = providers.stream()
                .filter(e -> bannedNodes == null ||
                        !bannedNodes.contains(e.replace("http://", "")
                                .replace("/", "")
                                .replace(".onion", "")))
                .collect(Collectors.toList());
    }
}
