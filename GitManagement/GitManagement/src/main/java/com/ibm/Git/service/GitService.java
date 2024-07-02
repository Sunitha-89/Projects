package com.ibm.Git.service;

import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public interface GitService {
	
	 public void validateAndcloneFromGit(String gitUrl, String branch, String user, String token, String localDirPath) throws Exception ;


}
