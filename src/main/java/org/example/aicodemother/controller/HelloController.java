package org.example.aicodemother.controller;

import org.example.aicodemother.common.BaseResponse;
import org.example.aicodemother.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping public BaseResponse<String> hello() {
        return ResultUtils.success("hello");
    }
}
