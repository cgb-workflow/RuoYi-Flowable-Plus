package com.ruoyi.web.controller.workflow;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.workflow.domain.dto.FlowSaveXmlVo;
import com.ruoyi.workflow.domain.vo.FlowDefinitionVo;
import com.ruoyi.workflow.service.IFlowDefinitionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 工作流程定义
 * </p>
 *
 * @author KonBAI
 * @date 2022-01-17
 */
@Slf4j
@Api(tags = "流程定义")
@RestController
@RequestMapping("/workflow/definition")
public class FlowDefinitionController extends BaseController {

    @Autowired
    private IFlowDefinitionService flowDefinitionService;

    @Autowired
    private ISysUserService userService;

    @Resource
    private ISysRoleService sysRoleService;


    @GetMapping(value = "/list")
    @ApiOperation(value = "流程定义列表", response = FlowDefinitionVo.class)
    public TableDataInfo<FlowDefinitionVo> list(PageQuery pageQuery) {
        return flowDefinitionService.list(pageQuery);
    }

    /**
     * 列出指定流程的发布版本列表
     *
     * @param processKey 流程定义Key
     * @return
     */
    @GetMapping(value = "/publishList")
    @ApiOperation(value = "流程定义列表", response = FlowDefinitionVo.class)
    public TableDataInfo<FlowDefinitionVo> publishList(@ApiParam(value = "流程定义Key", required = true) @RequestParam String processKey,
                                                       PageQuery pageQuery) {
        return flowDefinitionService.publishList(processKey, pageQuery);
    }


    @ApiOperation(value = "导入流程文件", notes = "上传bpmn20的xml文件")
    @PostMapping("/import")
    public R<Void> importFile(@RequestParam(required = false) String name,
                              @RequestParam(required = false) String category,
                              MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            flowDefinitionService.importFile(name, category, in);
        } catch (Exception e) {
            log.error("导入失败:", e);
            return R.fail(e.getMessage());
        }

        return R.ok("导入成功");
    }


    @ApiOperation(value = "读取xml文件")
    @GetMapping("/readXml/{definitionId}")
    public R<String> readXml(@ApiParam(value = "流程定义ID") @PathVariable(value = "definitionId") String definitionId) {
        try {
            return R.ok(null, flowDefinitionService.readXml(definitionId));
        } catch (Exception e) {
            return R.fail("加载xml文件异常");
        }

    }

    @ApiOperation(value = "读取图片文件")
    @GetMapping("/readImage/{definitionId}")
    public void readImage(@ApiParam(value = "流程定义id") @PathVariable(value = "definitionId") String definitionId,
                          HttpServletResponse response) {
        try (OutputStream os = response.getOutputStream()) {
            BufferedImage image = ImageIO.read(flowDefinitionService.readImage(definitionId));
            response.setContentType("image/png");
            if (image != null) {
                ImageIO.write(image, "png", os);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @ApiOperation(value = "保存流程设计器内的xml文件")
    @PostMapping("/save")
    public R<Void> save(@RequestBody FlowSaveXmlVo vo) {
        try (InputStream in = new ByteArrayInputStream(vo.getXml().getBytes(StandardCharsets.UTF_8))) {
            flowDefinitionService.importFile(vo.getName(), vo.getCategory(), in);
        } catch (Exception e) {
            log.error("导入失败:", e);
            return R.ok(e.getMessage());
        }

        return R.ok("导入成功");
    }


    @ApiOperation(value = "根据流程定义id启动流程实例")
    @PostMapping("/start/{procDefId}")
    public R<Void> start(@ApiParam(value = "流程定义id") @PathVariable(value = "procDefId") String procDefId,
                         @ApiParam(value = "变量集合,json对象") @RequestBody Map<String, Object> variables) {
        flowDefinitionService.startProcessInstanceById(procDefId, variables);
        return R.ok("流程启动成功");

    }

    @ApiOperation(value = "激活或挂起流程定义")
    @PutMapping(value = "/updateState")
    public R<Void> updateState(@ApiParam(value = "ture:挂起,false:激活", required = true) @RequestParam Boolean suspended,
                               @ApiParam(value = "流程定义ID", required = true) @RequestParam String definitionId) {
        flowDefinitionService.updateState(suspended, definitionId);
        return R.ok();
    }

    @ApiOperation(value = "删除流程")
    @DeleteMapping(value = "/delete")
    public R<Void> delete(@ApiParam(value = "流程部署ID", required = true) @RequestParam String deployId) {
        flowDefinitionService.delete(deployId);
        return R.ok();
    }

    @ApiOperation(value = "指定流程办理人员列表")
    @GetMapping("/userList")
    public R<List<SysUser>> userList(SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        return R.ok(list);
    }

    @ApiOperation(value = "指定流程办理组列表")
    @GetMapping("/roleList")
    public R<List<SysRole>> roleList(SysRole role) {
        List<SysRole> list = sysRoleService.selectRoleList(role);
        return R.ok(list);
    }

}
