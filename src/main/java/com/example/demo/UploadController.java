package com.example.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.entities.Visit;
import com.example.entities.Website;
import com.example.helpers.CsvParser;
import com.example.repositories.IVisitRepository;
import com.example.repositories.IWebsiteRepository;
import com.example.utils.Utils;

@RestController
public class UploadController {

	@Autowired
	private IWebsiteRepository webSiteRepository;
	@Autowired
	private IVisitRepository visitRepository;
	
	@RequestMapping(
            value = "/upload",
            method = RequestMethod.POST
    )
    public ResponseEntity uploadFile(MultipartHttpServletRequest request) {

        try {
            Iterator<String> itr = request.getFileNames();

            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                String filename = Utils.saveFile(request.getFile(uploadedFile));
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                List<String[]> dataArray = CsvParser.parse(filename, "|");
            	for(String[] data:dataArray) {
            		if(data.length == 3) {
            			try {
                			Date date = dateFormat.parse(data[0]);
                			String url = data[1];
                			long count = Long.parseLong(data[2]);
                			Website ws = this.webSiteRepository.findByUrl(url);
                			if(ws == null) {
                    			ws = new Website(url);
                    			ws = this.webSiteRepository.save(ws);
                			}
                			if(ws != null) {
                    			Visit visit = new Visit(count, date, ws);
                    			visit = this.visitRepository.save(visit);
                			}
            			}
            			catch(ParseException e) {
            				System.out.println(e.toString());
            			}
            			catch(NumberFormatException e) {
            				System.out.println(e.toString());
            			}
            			
            		}
            	}
            	Utils.deleteFile(filename);
            }
        }
        catch (Exception e) {
            return new ResponseEntity<>("{}", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
