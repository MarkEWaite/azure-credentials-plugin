/*
Copyright 2021 Tim Jacomb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.microsoft.jenkins.credentials;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.microsoft.azure.util.AzureBaseCredentials;
import com.microsoft.azure.util.AzureCredentialUtil;
import com.microsoft.azure.util.AzureCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Util;
import io.jenkins.plugins.azuresdk.HttpClientRetriever;

public final class AzureResourceManagerRetriever {

    private AzureResourceManagerRetriever() {}

    public static AzureResourceManager getClient(String credentialId, @CheckForNull String subscriptionId) {
        AzureBaseCredentials credential = AzureCredentialUtil.getCredential(null, credentialId);

        String actualSubscriptionId = subscriptionId != null ? subscriptionId : credential.getSubscriptionId();
        return getAzureResourceManager(credential, actualSubscriptionId);
    }

    private static AzureResourceManager getAzureResourceManager(
            AzureBaseCredentials azureCredentials, @CheckForNull String subscriptionId) {
        AzureProfile profile = new AzureProfile(azureCredentials.getAzureEnvironment());
        TokenCredential tokenCredential = AzureCredentials.getTokenCredential(azureCredentials);

        AzureResourceManager.Authenticated builder = AzureResourceManager.configure()
                .withHttpClient(HttpClientRetriever.get())
                .authenticate(tokenCredential, profile);

        if (Util.fixEmpty(subscriptionId) == null) {
            return builder.withDefaultSubscription();
        }

        return builder.withSubscription(subscriptionId);
    }
}
