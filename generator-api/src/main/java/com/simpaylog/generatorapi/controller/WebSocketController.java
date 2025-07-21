package com.simpaylog.generatorapi.controller;

import com.simpaylog.generatorapi.dto.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    @MessageMapping("/send")
    @SendTo("/sub/messages")
    public Response<Void> handleMessage(String message) throws Exception{
        return Response.success(HttpStatus.OK.value());
    }
}