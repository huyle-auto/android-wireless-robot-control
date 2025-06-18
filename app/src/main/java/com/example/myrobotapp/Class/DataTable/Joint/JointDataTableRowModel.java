package com.example.myrobotapp.Class.DataTable.Joint;

public class JointDataTableRowModel {
    private String no, j1, j2, j3, j4, j5, j6;

    // CONSTRUCTOR
    public JointDataTableRowModel(String no, String j1, String j2, String j3, String j4, String j5, String j6) {
        this.no = no;
        this.j1 = j1;
        this.j2 = j2;
        this.j3 = j3;
        this.j4 = j4;
        this.j5 = j5;
        this.j6 = j6;
    }

    // GETTER
    public String getNo() {
        return no;
    }

    public String getJ1() {
        return j1;
    }

    public String getJ2() {
        return j2;
    }

    public String getJ3() {
        return j3;
    }

    public String getJ4() {
        return j4;
    }

    public String getJ5() {
        return j5;
    }

    public String getJ6() {
        return j6;
    }

    //SETTER
    public void setNo(String no) {
        this.no = no;
    }

    public void setJ1(String j1) {
        this.j1 = j1;
    }

    public void setJ2(String j2) {
        this.j2 = j2;
    }

    public void setJ3(String j3) {
        this.j3 = j3;
    }

    public void setJ4(String j4) {
        this.j4 = j4;
    }

    public void setJ5(String j5) {
        this.j5 = j5;
    }

    public void setJ6(String j6) {
        this.j6 = j6;
    }
}
