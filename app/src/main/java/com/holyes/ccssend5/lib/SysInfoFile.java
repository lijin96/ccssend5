package com.holyes.ccssend5.lib;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @ClassName: SysInfoFile
 * @Description: 存储和保存一些数据-RFID相关的一些设置在200行之后
 * @Author: lijin
 * @Date: 2021/3/6 14:29
 */
public class SysInfoFile {

        Context mContext = null;
        String _file=null;

        // 获取当前程序路径getApplicationContext().getFilesDir().getAbsolutePath();
        // 获取该程序的安装包路径String path=getApplicationContext().getPackageResourcePath();
        // 获取程序默认数据库路径getApplicationContext().getDatabasePath(s).getAbsolutePath();

        public SysInfoFile(Context context) {
            mContext = context;
            _file = mContext.getApplicationContext().getFilesDir().getAbsolutePath()
                    + "/sysinfo.ini";
        }

        public String ReadParament(String paramentname)
        {
            File file = new File(_file);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                    //ShowMessage.Show(mContext, "指定文本文件:" + _file + "不存在！");
                }
                String strBuffer = "";
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null)
                    {
                        strBuffer += line;
                    }
                    instream.close();
                    if(strBuffer.isEmpty()) strBuffer = "[]";
                    JSONArray listjson = new JSONArray(strBuffer);
                    if (listjson.length() == 0)
                        return "";
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        if (jsonObject2.getString("Item").equals(paramentname)) {
                            return jsonObject2.getString("Value");
                        }
                    }

                }

            } catch (java.io.FileNotFoundException e) {
                ShowMessage.Show(mContext, e.getMessage());
                return "";
            } catch (IOException e) {
                ShowMessage.Show(mContext, e.getMessage());
                return "";
            }
            catch(org.json.JSONException e)
            {
                ShowMessage.Show(mContext,  e.getMessage());
            }
            return "";
        }

        public String ReadParament(String paramentname,String defultValue) {
            File file = new File(_file);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                    //ShowMessage.Show(mContext, "指定文本文件:" + _file + "不存在！");
                }
                String strBuffer = "";
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        strBuffer += line;
                    }
                    instream.close();
                    if(strBuffer.isEmpty()) strBuffer = "[]";
                    JSONArray listjson = new JSONArray(strBuffer);
                    if (listjson.length() == 0)
                        return defultValue;
                    for (int i = 0; i < listjson.length(); i++) {
                        JSONObject jsonObject2 = (JSONObject) listjson.opt(i);
                        if (jsonObject2.getString("Item").equals(paramentname)) {
                            return jsonObject2.getString("Value");
                        }
                    }

                }

            } catch (java.io.FileNotFoundException e) {
                ShowMessage.Show(mContext, e.getMessage());
                return "ERR";
            } catch (IOException e) {
                ShowMessage.Show(mContext, e.getMessage());
                return "ERR";
            }
            catch(org.json.JSONException e)
            {
                ShowMessage.Show(mContext,  e.getMessage());
            }
            return defultValue;
        }

        public void SaveParament(String paramentname, String value) {
            File file = new File(_file);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                String strBuffer = "";
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        strBuffer += line;
                    }
                    instream.close();
                    JSONObject jsonObject2 = null;
                    JSONArray listjson = null;
                    if(strBuffer.isEmpty())
                    {
                        strBuffer = "[]";
                    }

                    listjson = new JSONArray(strBuffer);
                    Boolean ifFound = false;
                    strBuffer = "";
                    for (int i = 0; i < listjson.length(); i++) {
                        jsonObject2 = (JSONObject) listjson.opt(i);
                        if (jsonObject2.getString("Item").equals(paramentname)) {
                            // ((JSONObject) listjson.opt(i)).putOpt("Value",value);
                            jsonObject2.put("Value", value);
                            ifFound = true;
                        }
                        strBuffer += (strBuffer.isEmpty() ? "" : ",")
                                + jsonObject2.toString() + "\n";
                        // list.add( jsonObject2.toString());
                    }
                    if (!ifFound) {
                        jsonObject2 = new JSONObject();
                        jsonObject2.put("Item", paramentname);
                        jsonObject2.put("Value", value);
                        strBuffer += (strBuffer.isEmpty() ? "" : ",")
                                + jsonObject2.toString() + "\n";
                    }
                    // String[] stringArray = list.toArray(new String[list.size()]);

                    try {
                        OutputStream outstream = new FileOutputStream(file);
                        OutputStreamWriter out = new OutputStreamWriter(outstream);
                        out.write("["+strBuffer+"]");
                        out.close();
                    } catch (java.io.IOException e) {
                        ShowMessage.Show(mContext, "写入操作:" + e.getMessage());
                    }

                }
            } catch (java.io.FileNotFoundException e) {
                ShowMessage.Show(mContext, e.getMessage());
            } catch (IOException e) {
                ShowMessage.Show(mContext,  e.getMessage());
            }
            catch(org.json.JSONException e)
            {
                ShowMessage.Show(mContext,  e.getMessage());
            }
            return;
        }

//RFID阅读器设置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//RFID阅读器设置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//RFID阅读器设置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        /**
         * RFID是否截取条码F，true：截取，false:不截取
         * @return
         */
        public boolean getIsSubBarcode()
        {
            return true;
        }

        /**
         * 与RFID通信的Socket的端口号
         */
        public int getSocketPort()
        {
            return 7778;
        }

        /**
         * 与RFID通信的Socket的端口号2
         */
        public int getSocketPort2()
        {
            return 8888;
        }


    }

