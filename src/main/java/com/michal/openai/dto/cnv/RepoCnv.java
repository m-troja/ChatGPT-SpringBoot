package com.michal.openai.dto.cnv;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.michal.openai.dto.BranchDto;
import com.michal.openai.dto.GithubRepoDto;
import com.michal.openai.entity.GithubBranch;
import com.michal.openai.entity.GithubRepo;

@Service
public class RepoCnv {
	
	public GithubRepoDto convertRepoToRepoDto(GithubRepo repo)
	{
		GithubRepoDto repoDto = new GithubRepoDto();
		repoDto.setOwnerLogin(repo.getOwner().getLogin());
		repoDto.setRepositoryName(repo.getName());
		List<BranchDto> branchDtos = new ArrayList<>();
		
		for (GithubBranch branch : repo.getBranches()) {
			BranchDto branchDto = new BranchDto(branch.getName(), branch.getCommit().getSha());
			branchDtos.add(branchDto);
		}
		
		repoDto.setBranches(branchDtos);
		
		return repoDto;
		
	}
	
	public List<GithubRepoDto> convertReposToRepoDtos(List<GithubRepo> repos)
	{
		List<GithubRepoDto> repoDtos = new ArrayList<>();
		
		for (GithubRepo repo : repos)
		{
			repoDtos.add(convertRepoToRepoDto(repo));
		}
		
		return repoDtos;
	}

}
