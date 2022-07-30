package com.sparta.springcore.controller;

import com.sparta.springcore.dto.FolderRequestDto;
import com.sparta.springcore.model.Folder;
import com.sparta.springcore.model.User;
import com.sparta.springcore.security.UserDetailsImpl;
import com.sparta.springcore.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FolderController {
    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping("api/folders")
    public List<Folder> addFolders(   //보통은 Dto형태로 만들어서 반환하는게 좋다.
          @RequestBody FolderRequestDto folderRequestDto,
          @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {


//        Long userId =  userDetails.getUser().getId(); // 원래 이렇게 해야하는데
        User user = userDetails.getUser();    // 객체의 연관관계를 통해서 이렇게 user를 넘겨줘도 됨.
        List<Folder> folders = folderService.addFolders(folderRequestDto.getFolderNames(), user);
        return folders;
    }
    @GetMapping("api/folders")
    public List<Folder> getFolders(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<Folder> folderList = folderService.getFolders(userDetails.getUser());
        return folderList;
    }
}
