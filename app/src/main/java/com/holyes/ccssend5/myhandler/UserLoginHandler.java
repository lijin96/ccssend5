package com.holyes.ccssend5.myhandler;

import com.holyes.ccssend5.entity.User;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Stack;
import java.util.Vector;

/**
 * @ClassName: UserLoginHandler
 * @Description: java类作用描述
 * @Author: lijin
 * @Date: 2021/3/6 14:31
 */
public class UserLoginHandler extends BaseHandler {


        @Override
        public boolean parse(String xmlString) {

            try {
                super.parserXml(this, xmlString);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private static final String tag = "UserLoginHandler";

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {

            String chars = new String(ch, start, length).trim();
            if (chars != null) {
                String tagName = tagStack.peek();// 查看栈顶对象而不移除它
                User object = users.lastElement();
                if (tagName.equals("P00")) {
                    object.setP00(chars);
                } else if (tagName.equals("P01")) {
                    object.setP01(chars);
                } else if (tagName.equals("P02")) {
                    object.setP02(chars);
                } else if (tagName.equals("P03")) {
                    object.setP03(chars);
                } else if (tagName.equals("P04")) {
                    object.setP04(chars);
                } else if (tagName.equals("P05")) {
                    object.setP05(chars);
                } else if (tagName.equals("P06")) {
                    object.setP06(chars);
                }
            }
        }

        @Override
        public void endDocument() throws SAXException {

            hash.put("users", users);// 保存入hash，这里保存的是Vector对象
            users = null;// 没用了就清掉
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {

            tagStack.pop();// 移除栈顶对象并作为此函数的值返回该对象
        }

        @Override
        public void startDocument() throws SAXException {


        }

        private Stack<String> tagStack = new Stack<String>();
        private Vector<User> users = new Vector<User>();

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {

            if (qName.equals("Paraments")) {
                User object = new User();
                object.setP00(attributes.getValue("P00"));
                users.addElement(object);
            }
            tagStack.push(qName);
        }
    }

