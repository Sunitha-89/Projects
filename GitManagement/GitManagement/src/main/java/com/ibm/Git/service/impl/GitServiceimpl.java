package com.ibm.Git.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.Git.service.GitService;

public class GitServiceimpl implements GitService {

    private static final Logger logger = LoggerFactory.getLogger(GitServiceimpl.class);
    private static final String HTTPS = "https";
    private static final String HTTP = "http";

    public void validateAndcloneFromGit(String gitUrl, String branch, String user, String token, String localDirPath) throws Exception {
        UsernamePasswordCredentialsProvider upc = new UsernamePasswordCredentialsProvider(user, token);
        File localDir = new File(localDirPath);

        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        try (Git git = Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(localDir)
                .setBranch(branch)
                .setCredentialsProvider(upc)
                .call()) {
            
            StoredConfig config = git.getRepository().getConfig();
            config.setBoolean(HTTP, null, "sslVerify", false);
            config.setBoolean(HTTPS, null, "sslVerify", false);
            config.save();

            Collection<Ref> refs = git.lsRemoteRepository()
                    .setHeads(true)
                    .setRemote(gitUrl)
                    .setCredentialsProvider(upc)
                    .call();

            Map<String, Ref> gitBranchMap = new HashMap<>();
            refs.forEach(ref -> {
                String branchName = ref.getName().substring(ref.getName().lastIndexOf("/") + 1);
                gitBranchMap.put(branchName, ref);
            });
            logger.info("Fetched branches: {}", gitBranchMap.values());

            if (gitBranchMap.containsKey(branch)) {
                logger.info("Branch exists. Cloned to local repository successfully.");
                commitToBranch(git, branch, "Initial commit", user, token);
            } else {
                logger.error("Branch does not exist.");
                throw new Exception("Branch does not exist.");
            }
        } catch (GitAPIException | IOException e) {
            logger.error("Error during Git operations: {}", e.getMessage());
            throw new Exception("Git operation failed.", e);
        }
    }

    public void commitToBranch(Git git, String branch, String commitMessage, String user, String token) throws Exception {
        try {
            // Stage all files
            git.add().addFilepattern(".").call();

            // Commit changes
            git.commit().setMessage(commitMessage).call();
            logger.info("Committed changes to branch {}", branch);

            // Push changes
            UsernamePasswordCredentialsProvider upc = new UsernamePasswordCredentialsProvider(user, token);
            Iterable<PushResult> pushResults = git.push()
                    .setCredentialsProvider(upc)
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec(branch))
                    .call();
            
            for (PushResult pushResult : pushResults) {
                for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
                    if (update.getStatus() == RemoteRefUpdate.Status.OK) {
                        logger.info("Pushed changes to branch {}", branch);
                    } else {
                        logger.error("Failed to push changes: {}", update.getStatus());
                        throw new Exception("Failed to push changes: " + update.getStatus());
                    }
                }
            }
        } catch (GitAPIException e) {
            logger.error("Error committing changes: {}", e.getMessage());
            throw new Exception("Commit operation failed.", e);
        }
    }
}
