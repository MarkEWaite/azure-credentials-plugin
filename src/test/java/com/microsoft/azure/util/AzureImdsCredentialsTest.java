package com.microsoft.azure.util;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.AccessDeniedException3;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;

public class AzureImdsCredentialsTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void descriptorVerifyConfigurationAsAdmin() {
        // No security realm, anonymous has Overall/Administer
        final AzureImdsCredentials.DescriptorImpl descriptor = new AzureImdsCredentials.DescriptorImpl();

        FormValidation result = descriptor.doVerifyConfiguration(null,"", "", "");
        Assert.assertEquals(FormValidation.Kind.ERROR, result.kind);
    }

    @Test
    public void descriptorVerifyConfigurationWithAncestorAsAuthorizedUser() throws Exception {
        Folder folder = j.jenkins.createProject(Folder.class, "folder");
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy authorizationStrategy = new MockAuthorizationStrategy();
        authorizationStrategy.grant(Jenkins.READ).everywhere().to("user");
        authorizationStrategy.grant(Item.CONFIGURE).onFolders(folder).to("user");
        j.jenkins.setAuthorizationStrategy(authorizationStrategy);

        final AzureImdsCredentials.DescriptorImpl descriptor = new AzureImdsCredentials.DescriptorImpl();

        try (ACLContext ctx = ACL.as(User.getOrCreateByIdOrFullName("user"))) {
            FormValidation result = descriptor.doVerifyConfiguration(folder, "", "", "");
            // we aren't looking up an actual secret so this fails with missing protocol
            // TODO mock secrets retrieval so we can test the happy case here properly
            Assert.assertEquals(FormValidation.Kind.ERROR, result.kind);
        }
    }

    @Test
    public void descriptorVerifyConfigurationWithAncestorAsUnauthorizedUser() throws Exception {
        Folder folder = j.jenkins.createProject(Folder.class, "folder");
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        MockAuthorizationStrategy authorizationStrategy = new MockAuthorizationStrategy();
        authorizationStrategy.grant(Jenkins.READ).everywhere().to("user");
        j.jenkins.setAuthorizationStrategy(authorizationStrategy);

        final AzureImdsCredentials.DescriptorImpl descriptor = new AzureImdsCredentials.DescriptorImpl();

        try (ACLContext ctx = ACL.as(User.getOrCreateByIdOrFullName("user"))) {
            Assert.assertThrows(AccessDeniedException3.class, () -> descriptor.doVerifyConfiguration(folder, "", "", ""));
        }
    }
}
