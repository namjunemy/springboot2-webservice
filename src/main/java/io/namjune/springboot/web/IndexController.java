package io.namjune.springboot.web;

import io.namjune.springboot.config.auth.LoginUser;
import io.namjune.springboot.config.auth.dto.SessionUser;
import io.namjune.springboot.service.posts.PostsService;
import io.namjune.springboot.web.dto.PostsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model) {
        PostsResponseDto responseDto = postsService.findById(id);
        model.addAttribute("post", responseDto);
        return "posts-update";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser sessionUser) {
        model.addAttribute("posts", postsService.findAllDesc());
        if (sessionUser != null) {
            model.addAttribute("userName", sessionUser.getName());
        }
        return "index";
    }
}
