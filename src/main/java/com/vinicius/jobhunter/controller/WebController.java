package com.vinicius.jobhunter.controller;

import com.vinicius.jobhunter.model.Job;
import com.vinicius.jobhunter.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final JobRepository jobRepository;

    @GetMapping("/")
    public String index(Model model) {
        List<Job> jobs = jobRepository.findAll(Sort.by(Sort.Direction.DESC, "score"));
        model.addAttribute("jobs", jobs);
        return "index";
    }
}
