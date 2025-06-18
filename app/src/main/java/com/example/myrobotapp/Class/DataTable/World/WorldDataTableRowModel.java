package com.example.myrobotapp.Class.DataTable.World;

public class WorldDataTableRowModel {
    private String no, x, y, z, rX, rY, rZ;

    // CONSTRUCTOR
    public WorldDataTableRowModel(String no, String x, String y, String z, String rX, String rY, String rZ) {
        this.no = no;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rX = rX;
        this.rY = rY;
        this.rZ = rZ;
    }

    // GETTER
    public String getNo() {
        return no;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public String getZ() {
        return z;
    }

    public String getRx() {
        return rX;
    }

    public String getRy() {
        return rY;
    }

    public String getRz() {
        return rZ;
    }

    //SETTER
    public void setNo(String no) {
        this.no = no;
    }

    public void setX(String x) {
        this.x = x;
    }

    public void setY(String y) {
        this.y = y;
    }

    public void setZ(String z) {
        this.z = z;
    }

    public void setRx(String rX) {
        this.rX = rX;
    }

    public void setRy(String rY) {
        this.rY = rY;
    }

    public void setRz(String rZ) {
        this.rZ = rZ;
    }
}
