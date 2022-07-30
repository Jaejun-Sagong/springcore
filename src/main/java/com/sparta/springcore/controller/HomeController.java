package com.sparta.springcore.controller;

import com.sparta.springcore.model.Folder;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.repository.FolderRepository;
import com.sparta.springcore.security.UserDetailsImpl;
import com.sparta.springcore.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final FolderService folderService;

    @Autowired
    public HomeController(FolderService folderService){
        this.folderService = folderService;
    }

//    @Autowired
//    public HomeController(FolderRepository folderRepository) {   //Repo에서 가져와도 되지만 Service를 통해서 가져오는 것이 좀 더 역할을 지키는 것이라 할 수 있다.
//        this.folderRepository = folderRepository;
//    }

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        model.addAttribute("username", userDetails.getUsername());

        if (userDetails.getUser().getRole() == UserRoleEnum.ADMIN) {    //UserRoleEnum
            model.addAttribute("admin_role", true);
        }

        List<Folder> folderList = folderService.getFolders(userDetails.getUser());
        model.addAttribute("folders", folderList);

        return "index";
    }
}
