package com.gos.monitor.server.manager.controller;

import com.gos.monitor.server.manager.NormalResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by xue on 2017-05-27.
 */
@Controller
public class IndexController {

    @RequestMapping(path = {"/funcs"})
    @ResponseBody
    public Object funcs() {
        return NormalResponse.create().ok().put("", "");
    }
}
