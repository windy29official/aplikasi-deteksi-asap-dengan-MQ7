package aplikasiispu.rajacoding.com;

public class LaporanModel {
    private String idasap;
    private String asap;
    private String tanggal;

    public LaporanModel(String idasap, String asap, String tanggal) {
        this.idasap = idasap;
        this.asap = asap;
        this.tanggal = tanggal;
    }

    public String getIdasap() {
        return idasap;
    }

    public String getAsap() {
        return asap;
    }

    public String getTanggal() {
        return tanggal;
    }
}
