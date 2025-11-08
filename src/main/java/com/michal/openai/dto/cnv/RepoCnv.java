package com.michal.openai.dto.cnv;

import com.michal.openai.dto.BranchDto;
import com.michal.openai.dto.GithubRepoDto;
import com.michal.openai.entity.GithubBranch;
import com.michal.openai.entity.GithubRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RepoCnv {
	
	public GithubRepoDto convertRepoToRepoDto(GithubRepo repo)
	{
		List<BranchDto>  branchDtos = new ArrayList<>();

		for (GithubBranch branch : repo.branches()) {
			BranchDto branchDto = new BranchDto(branch.name(), branch.commit().sha());
			branchDtos.add(branchDto);
		}
        GithubRepoDto repoDto = new GithubRepoDto(repo.owner().login(), repo.name(), branchDtos);
		return repoDto;
	}
	
	public List<GithubRepoDto> convertReposToRepoDtos(List<GithubRepo> repos)
	{
		List<GithubRepoDto> repoDtos = new ArrayList<>();
		
		for (GithubRepo repo : repos) {
			repoDtos.add(convertRepoToRepoDto(repo));
		}
		
		return repoDtos;
	}
}
