package cn.mipos.service;

/**
 * Created by jarry on 16/9/29.
 */
public class PrinterStatus {
    public int jobResult;                //last job result: 0-finish, other
    //not finish for reason:  1-out paper, 2-data error, 3-force to stop
    public int jobDataCount;            //last job: need to print data
    public int jobPrintedCount;        //last job: has printed data
    public int printerStatusHeat;    //printer status:	0-OK, 1-over heat
    public int printerStatusPaper;    //printer status:  	0-OK, 1-out paper

    public int getJobResult() {
        return jobResult;
    }

    public void setJobResult(int jobResult) {
        this.jobResult = jobResult;
    }

    public int getJobDataCount() {
        return jobDataCount;
    }

    public void setJobDataCount(int jobDataCount) {
        this.jobDataCount = jobDataCount;
    }

    public int getJobPrintedCount() {
        return jobPrintedCount;
    }

    public void setJobPrintedCount(int jobPrintedCount) {
        this.jobPrintedCount = jobPrintedCount;
    }

    public int getPrinterStatusHeat() {
        return printerStatusHeat;
    }

    public void setPrinterStatusHeat(int printerStatusHeat) {
        this.printerStatusHeat = printerStatusHeat;
    }

    public int getPrinterStatusPaper() {
        return printerStatusPaper;
    }

    public void setPrinterStatusPaper(int printerStatusPaper) {
        this.printerStatusPaper = printerStatusPaper;
    }
}
