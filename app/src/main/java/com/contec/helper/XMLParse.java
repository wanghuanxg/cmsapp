package com.contec.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


import android.util.Xml;

public class XMLParse {

	private static final String ENCODING = "GBK";


	/**
	 * 解析客户端登录时专家信息的xml文件
	 * 
	 * @throws org.xmlpull.v1.XmlPullParserException
	 * @throws java.io.IOException
	 */
	public UserInfo parseUser(String userFilePath) throws XmlPullParserException, IOException {

		XmlPullParser parser = Xml.newPullParser(); // 由android.util.Xml创建一个XmlPullParser实例
		InputStream is = new FileInputStream(userFilePath);
		parser.setInput(is, ENCODING); // 设置输入?并指明编码方?
		UserInfo user = null;

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				user = new UserInfo();
				break;
			case XmlPullParser.START_TAG:
				if (("id").equals(parser.getName())) {
					eventType = parser.next();
					user.setId(parser.getText());
				} else if (("uid").equals(parser.getName())) {
					eventType = parser.next();
					user.setUid(parser.getText());
				} else if (("senderid").equals(parser.getName())) {
					eventType = parser.next();
					user.setSenderid(parser.getText());
				}else if (("pid").equals(parser.getName())) {
					eventType = parser.next();
					user.setPid(parser.getText());
				}else if (("name").equals(parser.getName())) {
					eventType = parser.next();
					user.setName(parser.getText());
				}else if (("tel").equals(parser.getName())) {
					eventType = parser.next();
					user.setTel(parser.getText());
				}else if (("notes").equals(parser.getName())) {
					eventType = parser.next();
					user.setNotes(parser.getText());
				}else if (("area").equals(parser.getName())) {
					eventType = parser.next();
					user.setArea(parser.getText());
				}else if (("areaid").equals(parser.getName())) {
					eventType = parser.next();
					user.setAreaid(parser.getText());
				}else if (("createdate").equals(parser.getName())) {
					eventType = parser.next();
					user.setCreatedate(parser.getText());
				}else if (("hospitalid").equals(parser.getName())) {
					eventType = parser.next();
					user.setHospitalid(parser.getText());
				}else if (("hospitalname").equals(parser.getName())) {
					eventType = parser.next();
					user.setHospitalname(parser.getText());
				}else if (("transtype").equals(parser.getName())) {
					eventType = parser.next();
					user.setTranstype(parser.getText());
				}else if (("hgroupid").equals(parser.getName())) {
					eventType = parser.next();
					user.setHgroupid(parser.getText());
				}else if (("hgroupname").equals(parser.getName())) {
					eventType = parser.next();
					user.setHgroupname(parser.getText());
				}else if (("sid").equals(parser.getName())) {
					eventType = parser.next();
					user.setSid(parser.getText());
				}
				else if (("anotherlogininfo").equals(parser.getName())) {
					eventType = parser.next();
					user.setAnotherlogininfo(parser.getText());
				}
				break;
			case XmlPullParser.END_TAG:
				break;
			}
			eventType = parser.next();
		}
		return user;
	}
    /**
     * 解析version版本更新xml文件
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    public Versioninfo parseVersionXml(String versionXMlFilePath) throws XmlPullParserException, IOException {
        Versioninfo versioninfo = null;
        XmlPullParser parser = Xml.newPullParser(); // 由android.util.Xml创建一个XmlPullParser实例
        InputStream is = new FileInputStream(versionXMlFilePath);
        parser.setInput(is, ENCODING); // 设置输入?并指明编码方?

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (("file").equals(parser.getName())) {
                        versioninfo = new Versioninfo();
                    } else if (("fname").equals(parser.getName())) {
                        eventType = parser.next();
                        versioninfo.setFname(parser.getText());
                    } else if (("path").equals(parser.getName())) {
                        eventType = parser.next();
                        versioninfo.setPath(parser.getText());
                    } else if (("version").equals(parser.getName())) {
                        eventType = parser.next();
                        versioninfo.setVersion(parser.getText());
                    } else if (("type").equals(parser.getName())) {
                        eventType = parser.next();
                        versioninfo.setType(parser.getText());
                    }else if (("size").equals(parser.getName())) {
                        eventType = parser.next();
                        versioninfo.setSize(Long.parseLong(parser.getText().trim()));
                    }
                    break;

                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = parser.next();
        }

        return versioninfo;
    }
	// end
}
