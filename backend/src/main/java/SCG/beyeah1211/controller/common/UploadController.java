package SCG.beyeah1211.controller.common;

import SCG.beyeah1211.common.Constants;
import SCG.beyeah1211.util.Result;
import SCG.beyeah1211.util.ResultGenerator;
import SCG.beyeah1211.util.beyeahUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/admin")
public class UploadController {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    private static final int MAX_BATCH_UPLOAD = 5;

    @Autowired
    private StandardServletMultipartResolver standardServletMultipartResolver;

    @PostMapping({"/upload/file"})
    @ResponseBody
    public Result upload(HttpServletRequest httpServletRequest, @RequestParam("file") MultipartFile file)
            throws URISyntaxException, IOException {
        String fileName = file.getOriginalFilename();
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        if (bufferedImage == null) {
            return ResultGenerator.genFailResult("only image file is allowed");
        }
        String suffixName = fileName.substring(fileName.lastIndexOf('.'));
        String newFileName = buildFileName(suffixName);
        File fileDirectory = new File(Constants.FILE_UPLOAD_DIC);
        File destFile = new File(fileDirectory, newFileName);
        try {
            ensureUploadDirExists(fileDirectory);
            file.transferTo(destFile);
            Result resultSuccess = ResultGenerator.genSuccessResult();
            resultSuccess.setData(beyeahUtils.getHost(new URI(httpServletRequest.getRequestURL() + "")) + "/upload/" + newFileName);
            return resultSuccess;
        } catch (IOException e) {
            logger.error("single file upload failed, file={}", fileName, e);
            return ResultGenerator.genFailResult("file upload failed");
        }
    }

    @PostMapping({"/upload/files"})
    @ResponseBody
    public Result uploadV2(HttpServletRequest httpServletRequest) throws URISyntaxException, IOException {
        List<MultipartFile> multipartFiles = new ArrayList<>(8);
        if (standardServletMultipartResolver.isMultipart(httpServletRequest)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) httpServletRequest;
            Iterator<String> iter = multiRequest.getFileNames();
            int total = 0;
            while (iter.hasNext()) {
                if (total >= MAX_BATCH_UPLOAD) {
                    return ResultGenerator.genFailResult("at most 5 images per request");
                }
                total += 1;
                MultipartFile file = multiRequest.getFile(iter.next());
                if (file == null) {
                    continue;
                }
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                if (bufferedImage != null) {
                    multipartFiles.add(file);
                }
            }
        }
        if (CollectionUtils.isEmpty(multipartFiles)) {
            return ResultGenerator.genFailResult("please upload image files");
        }
        if (multipartFiles.size() > MAX_BATCH_UPLOAD) {
            return ResultGenerator.genFailResult("at most 5 images per request");
        }

        List<String> fileNames = new ArrayList<>(multipartFiles.size());
        for (MultipartFile multipartFile : multipartFiles) {
            String fileName = multipartFile.getOriginalFilename();
            String suffixName = fileName.substring(fileName.lastIndexOf('.'));
            String newFileName = buildFileName(suffixName);
            File fileDirectory = new File(Constants.FILE_UPLOAD_DIC);
            File destFile = new File(fileDirectory, newFileName);
            try {
                ensureUploadDirExists(fileDirectory);
                multipartFile.transferTo(destFile);
                fileNames.add(beyeahUtils.getHost(new URI(httpServletRequest.getRequestURL() + "")) + "/upload/" + newFileName);
            } catch (IOException e) {
                logger.error("batch file upload failed, file={}", fileName, e);
                return ResultGenerator.genFailResult("file upload failed");
            }
        }
        Result resultSuccess = ResultGenerator.genSuccessResult();
        resultSuccess.setData(fileNames);
        return resultSuccess;
    }

    private void ensureUploadDirExists(File fileDirectory) throws IOException {
        if (fileDirectory.exists()) {
            return;
        }
        if (!fileDirectory.mkdirs()) {
            throw new IOException("failed to create upload dir: " + fileDirectory);
        }
    }

    private String buildFileName(String suffixName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Random random = new Random();
        return sdf.format(new Date()) + random.nextInt(100) + suffixName;
    }
}

