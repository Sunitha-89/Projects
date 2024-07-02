package com.ibm.Git;

import java.io.File;
import com.ibm.Git.service.impl.GitServiceimpl;
import org.eclipse.jgit.api.Git;

public class App {
    public static void main(String[] args) {
        GitServiceimpl gitService = new GitServiceimpl();
        String gitUrl = "https://github.com/swarnaacharya/spring-config.git";
        String branch = "master"; // Specify the branch you want to pull
        String user = "swarnaacharya"; // Replace with your Git username
        String token = "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"; // Replace with your Git token
        String localDirPath = "C:\\Users\\Swarna\\Desktop\\New folder"; // Replace with the path to your local directory

        try {
            // Clone the repository
            gitService.validateAndcloneFromGit(gitUrl, branch, user, token, localDirPath);
            System.out.println("Repository cloned successfully.");

            // Reopen the cloned repository
            File localDir = new File(localDirPath);
            try (Git git = Git.open(localDir)) {
                // Commit changes to the branch
                gitService.commitToBranch(git, "test", "Test commit", user, token);
                System.out.println("Changes committed successfully.");
            } catch (Exception e) {
                System.err.println("Error during commit operation: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error during Git operation: " + e.getMessage());
        }
    }
}
