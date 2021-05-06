/**
 * 
 */
package com.holyes.ccssend5.entity;
/**
 * @author van van.shu@magic-point.com
 * @version ����ʱ�䣺2021-4-23 ����4:27:05
 * ��˵�� �����ױ��ʵ����
 */

public class PackMealBoxLabel {
	private String PackBoxNumber;//": "P20091700001",    ���б���)
	private String PackId;//": "200916000001",            (�ײʹ���)
	private String PackName;//": "�����ױ�",              (�ײ�����)
	private String PackNum;//": "6"                       (��װ����)
	/**
	 * @return the packBoxNumber
	 */
	public String getPackBoxNumber() {
		return PackBoxNumber;
	}
	/**
	 * @param packBoxNumber the packBoxNumber to set
	 */
	public void setPackBoxNumber(String packBoxNumber) {
		PackBoxNumber = packBoxNumber;
	}
	/**
	 * @return the packId
	 */
	public String getPackId() {
		return PackId;
	}
	/**
	 * @param packId the packId to set
	 */
	public void setPackId(String packId) {
		PackId = packId;
	}
	/**
	 * @return the packName
	 */
	public String getPackName() {
		return PackName;
	}
	/**
	 * @param packName the packName to set
	 */
	public void setPackName(String packName) {
		PackName = packName;
	}
	/**
	 * @return the packNum
	 */
	public String getPackNum() {
		return PackNum;
	}
	/**
	 * @param packNum the packNum to set
	 */
	public void setPackNum(String packNum) {
		PackNum = packNum;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PackMealBoxLabel [PackBoxNumber=" + PackBoxNumber + ", PackId="
				+ PackId + ", PackName=" + PackName + ", PackNum=" + PackNum
				+ "]";
	}
	/**
	 * @param packBoxNumber
	 * @param packId
	 * @param packName
	 * @param packNum
	 */
	public PackMealBoxLabel(String packBoxNumber, String packId,
			String packName, String packNum) {
		super();
		PackBoxNumber = packBoxNumber;
		PackId = packId;
		PackName = packName;
		PackNum = packNum;
	}
	/**
	 * 
	 */
	public PackMealBoxLabel() {
		super();
	}
	
}
