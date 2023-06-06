package huorong.entity;


import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author lzx
 * @date 2023/6/5 17:07
 * @description: TODO
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true) //开启链式调用
public class SampleUserdefinedScan {
    private String rk;
    private Long id;
    private String sha1;
    private String sha256;
    private String sha512;
    private String md5;
    private String simhash;
    private String src_list;
    private String die;
    private String fdfs_path;
    private String hashsig;
    private String hashsig_pe;
    private Long filesize;
    private String filetype;
    private Long lastaddtime;
    private Long addtime;
    private Long scan_time;
    // kafka
    private Long task_id;
    private String task_type;

    private String hr_scan_id;
    private String hr_scan_name;
    private String hr_scan_virus_name;
    private String hr_scan_virus_tech;
    private String hr_scan_virus_type;
    private String hr_scan_virus_platform;

    private String hr_test_scan_id;
    private String hr_test_scan_name;
    private String hr_test_scan_virus_name;
    private String hr_test_scan_virus_tech;
    private String hr_test_scan_virus_type;
    private String hr_test_scan_virus_platform;

    private String rel_scan_id;
    private String rel_scan_name;
    private String rel_scan_virus_name;
    private String rel_scan_virus_tech;
    private String rel_scan_virus_type;
    private String rel_scan_virus_platform;

    private String eset_scan_id;
    private String eset_scan_name;
    private String eset_scan_virus_name;
    private String eset_scan_virus_tech;
    private String eset_scan_virus_type;
    private String eset_scan_virus_platform;

    private String ms_scan_id;
    private String ms_scan_name;
    private String ms_scan_virus_name;
    private String ms_scan_virus_tech;
    private String ms_scan_virus_type;
    private String ms_scan_virus_platform;

    private String emu_scan_id;
    private String emu_scan_name;
    private String emu_scan_virus_name;
    private String emu_scan_virus_tech;
    private String emu_scan_virus_type;
    private String emu_scan_virus_platform;

    private String avp_scan_id;
    private String avp_scan_name;
    private String avp_scan_virus_name;
    private String avp_scan_virus_tech;
    private String avp_scan_virus_type;
    private String avp_scan_virus_platform;

    private String troj_scan_id;
    private String troj_scan_name;
    private String troj_scan_virus_name;
    private String troj_scan_virus_tech;
    private String troj_scan_virus_type;
    private String troj_scan_virus_platform;

    private String die_scan_id;
    private String die_scan_name;
    private String die_scan_virus_name;
    private String die_scan_virus_tech;
    private String die_scan_virus_type;
    private String die_scan_virus_platform;

    private String qvm_scan_id;
    private String qvm_scan_name;
    private String qvm_scan_virus_name;
    private String qvm_scan_virus_tech;
    private String qvm_scan_virus_type;
    private String qvm_scan_virus_platform;

    public void setRk(String rk) {
        this.rk = rk;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setSimhash(String simhash) {
        this.simhash = simhash;
    }

    public void setSrc_list(String src_list) {
        this.src_list = src_list;
    }

    public void setDie(String die) {
        this.die = die;
    }

    public void setFdfs_path(String fdfs_path) {
        this.fdfs_path = fdfs_path;
    }

    public void setHashsig(String hashsig) {
        this.hashsig = hashsig;
    }

    public void setHashsig_pe(String hashsig_pe) {
        this.hashsig_pe = hashsig_pe;
    }

    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public void setLastaddtime(Long lastaddtime) {
        this.lastaddtime = lastaddtime;
    }

    public void setAddtime(Long addtime) {
        this.addtime = addtime;
    }

    public void setScan_time(Long scan_time) {
        this.scan_time = scan_time;
    }

    public void setTask_id(Long task_id) {
        this.task_id = task_id;
    }

    public void setTask_type(String task_type) {
        this.task_type = task_type;
    }

    public void setHr_scan_id(String hr_scan_id) {
        this.hr_scan_id = hr_scan_id;
    }

    public void setHr_scan_name(String hr_scan_name) {
        this.hr_scan_name = hr_scan_name;
    }

    public void setHr_scan_virus_name(String hr_scan_virus_name) {
        this.hr_scan_virus_name = hr_scan_virus_name;
    }

    public void setHr_scan_virus_tech(String hr_scan_virus_tech) {
        this.hr_scan_virus_tech = hr_scan_virus_tech;
    }

    public void setHr_scan_virus_type(String hr_scan_virus_type) {
        this.hr_scan_virus_type = hr_scan_virus_type;
    }

    public void setHr_scan_virus_platform(String hr_scan_virus_platform) {
        this.hr_scan_virus_platform = hr_scan_virus_platform;
    }

    public void setHr_test_scan_id(String hr_test_scan_id) {
        this.hr_test_scan_id = hr_test_scan_id;
    }

    public void setHr_test_scan_name(String hr_test_scan_name) {
        this.hr_test_scan_name = hr_test_scan_name;
    }

    public void setHr_test_scan_virus_name(String hr_test_scan_virus_name) {
        this.hr_test_scan_virus_name = hr_test_scan_virus_name;
    }

    public void setHr_test_scan_virus_tech(String hr_test_scan_virus_tech) {
        this.hr_test_scan_virus_tech = hr_test_scan_virus_tech;
    }

    public void setHr_test_scan_virus_type(String hr_test_scan_virus_type) {
        this.hr_test_scan_virus_type = hr_test_scan_virus_type;
    }

    public void setHr_test_scan_virus_platform(String hr_test_scan_virus_platform) {
        this.hr_test_scan_virus_platform = hr_test_scan_virus_platform;
    }

    public void setRel_scan_id(String rel_scan_id) {
        this.rel_scan_id = rel_scan_id;
    }

    public void setRel_scan_name(String rel_scan_name) {
        this.rel_scan_name = rel_scan_name;
    }

    public void setRel_scan_virus_name(String rel_scan_virus_name) {
        this.rel_scan_virus_name = rel_scan_virus_name;
    }

    public void setRel_scan_virus_tech(String rel_scan_virus_tech) {
        this.rel_scan_virus_tech = rel_scan_virus_tech;
    }

    public void setRel_scan_virus_type(String rel_scan_virus_type) {
        this.rel_scan_virus_type = rel_scan_virus_type;
    }

    public void setRel_scan_virus_platform(String rel_scan_virus_platform) {
        this.rel_scan_virus_platform = rel_scan_virus_platform;
    }

    public void setEset_scan_id(String eset_scan_id) {
        this.eset_scan_id = eset_scan_id;
    }

    public void setEset_scan_name(String eset_scan_name) {
        this.eset_scan_name = eset_scan_name;
    }

    public void setEset_scan_virus_name(String eset_scan_virus_name) {
        this.eset_scan_virus_name = eset_scan_virus_name;
    }

    public void setEset_scan_virus_tech(String eset_scan_virus_tech) {
        this.eset_scan_virus_tech = eset_scan_virus_tech;
    }

    public void setEset_scan_virus_type(String eset_scan_virus_type) {
        this.eset_scan_virus_type = eset_scan_virus_type;
    }

    public void setEset_scan_virus_platform(String eset_scan_virus_platform) {
        this.eset_scan_virus_platform = eset_scan_virus_platform;
    }

    public void setMs_scan_id(String ms_scan_id) {
        this.ms_scan_id = ms_scan_id;
    }

    public void setMs_scan_name(String ms_scan_name) {
        this.ms_scan_name = ms_scan_name;
    }

    public void setMs_scan_virus_name(String ms_scan_virus_name) {
        this.ms_scan_virus_name = ms_scan_virus_name;
    }

    public void setMs_scan_virus_tech(String ms_scan_virus_tech) {
        this.ms_scan_virus_tech = ms_scan_virus_tech;
    }

    public void setMs_scan_virus_type(String ms_scan_virus_type) {
        this.ms_scan_virus_type = ms_scan_virus_type;
    }

    public void setMs_scan_virus_platform(String ms_scan_virus_platform) {
        this.ms_scan_virus_platform = ms_scan_virus_platform;
    }

    public void setEmu_scan_id(String emu_scan_id) {
        this.emu_scan_id = emu_scan_id;
    }

    public void setEmu_scan_name(String emu_scan_name) {
        this.emu_scan_name = emu_scan_name;
    }

    public void setEmu_scan_virus_name(String emu_scan_virus_name) {
        this.emu_scan_virus_name = emu_scan_virus_name;
    }

    public void setEmu_scan_virus_tech(String emu_scan_virus_tech) {
        this.emu_scan_virus_tech = emu_scan_virus_tech;
    }

    public void setEmu_scan_virus_type(String emu_scan_virus_type) {
        this.emu_scan_virus_type = emu_scan_virus_type;
    }

    public void setEmu_scan_virus_platform(String emu_scan_virus_platform) {
        this.emu_scan_virus_platform = emu_scan_virus_platform;
    }

    public void setAvp_scan_id(String avp_scan_id) {
        this.avp_scan_id = avp_scan_id;
    }

    public void setAvp_scan_name(String avp_scan_name) {
        this.avp_scan_name = avp_scan_name;
    }

    public void setAvp_scan_virus_name(String avp_scan_virus_name) {
        this.avp_scan_virus_name = avp_scan_virus_name;
    }

    public void setAvp_scan_virus_tech(String avp_scan_virus_tech) {
        this.avp_scan_virus_tech = avp_scan_virus_tech;
    }

    public void setAvp_scan_virus_type(String avp_scan_virus_type) {
        this.avp_scan_virus_type = avp_scan_virus_type;
    }

    public void setAvp_scan_virus_platform(String avp_scan_virus_platform) {
        this.avp_scan_virus_platform = avp_scan_virus_platform;
    }

    public void setTroj_scan_id(String troj_scan_id) {
        this.troj_scan_id = troj_scan_id;
    }

    public void setTroj_scan_name(String troj_scan_name) {
        this.troj_scan_name = troj_scan_name;
    }

    public void setTroj_scan_virus_name(String troj_scan_virus_name) {
        this.troj_scan_virus_name = troj_scan_virus_name;
    }

    public void setTroj_scan_virus_tech(String troj_scan_virus_tech) {
        this.troj_scan_virus_tech = troj_scan_virus_tech;
    }

    public void setTroj_scan_virus_type(String troj_scan_virus_type) {
        this.troj_scan_virus_type = troj_scan_virus_type;
    }

    public void setTroj_scan_virus_platform(String troj_scan_virus_platform) {
        this.troj_scan_virus_platform = troj_scan_virus_platform;
    }

    public void setDie_scan_id(String die_scan_id) {
        this.die_scan_id = die_scan_id;
    }

    public void setDie_scan_name(String die_scan_name) {
        this.die_scan_name = die_scan_name;
    }

    public void setDie_scan_virus_name(String die_scan_virus_name) {
        this.die_scan_virus_name = die_scan_virus_name;
    }

    public void setDie_scan_virus_tech(String die_scan_virus_tech) {
        this.die_scan_virus_tech = die_scan_virus_tech;
    }

    public void setDie_scan_virus_type(String die_scan_virus_type) {
        this.die_scan_virus_type = die_scan_virus_type;
    }

    public void setDie_scan_virus_platform(String die_scan_virus_platform) {
        this.die_scan_virus_platform = die_scan_virus_platform;
    }

    public void setQvm_scan_id(String qvm_scan_id) {
        this.qvm_scan_id = qvm_scan_id;
    }

    public void setQvm_scan_name(String qvm_scan_name) {
        this.qvm_scan_name = qvm_scan_name;
    }

    public void setQvm_scan_virus_name(String qvm_scan_virus_name) {
        this.qvm_scan_virus_name = qvm_scan_virus_name;
    }

    public void setQvm_scan_virus_tech(String qvm_scan_virus_tech) {
        this.qvm_scan_virus_tech = qvm_scan_virus_tech;
    }

    public void setQvm_scan_virus_type(String qvm_scan_virus_type) {
        this.qvm_scan_virus_type = qvm_scan_virus_type;
    }

    public void setQvm_scan_virus_platform(String qvm_scan_virus_platform) {
        this.qvm_scan_virus_platform = qvm_scan_virus_platform;
    }

    public String getRk() {
        return rk;
    }

    public Long getId() {
        return id;
    }

    public String getSha1() {
        return sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public String getSha512() {
        return sha512;
    }

    public String getMd5() {
        return md5;
    }

    public String getSimhash() {
        return simhash;
    }

    public String getSrc_list() {
        return src_list;
    }

    public String getDie() {
        return die;
    }

    public String getFdfs_path() {
        return fdfs_path;
    }

    public String getHashsig() {
        return hashsig;
    }

    public String getHashsig_pe() {
        return hashsig_pe;
    }

    public Long getFilesize() {
        return filesize;
    }

    public String getFiletype() {
        return filetype;
    }

    public Long getLastaddtime() {
        return lastaddtime;
    }

    public Long getAddtime() {
        return addtime;
    }

    public Long getScan_time() {
        return scan_time;
    }

    public Long getTask_id() {
        return task_id;
    }

    public String getTask_type() {
        return task_type;
    }

    public String getHr_scan_id() {
        return hr_scan_id;
    }

    public String getHr_scan_name() {
        return hr_scan_name;
    }

    public String getHr_scan_virus_name() {
        return hr_scan_virus_name;
    }

    public String getHr_scan_virus_tech() {
        return hr_scan_virus_tech;
    }

    public String getHr_scan_virus_type() {
        return hr_scan_virus_type;
    }

    public String getHr_scan_virus_platform() {
        return hr_scan_virus_platform;
    }

    public String getHr_test_scan_id() {
        return hr_test_scan_id;
    }

    public String getHr_test_scan_name() {
        return hr_test_scan_name;
    }

    public String getHr_test_scan_virus_name() {
        return hr_test_scan_virus_name;
    }

    public String getHr_test_scan_virus_tech() {
        return hr_test_scan_virus_tech;
    }

    public String getHr_test_scan_virus_type() {
        return hr_test_scan_virus_type;
    }

    public String getHr_test_scan_virus_platform() {
        return hr_test_scan_virus_platform;
    }

    public String getRel_scan_id() {
        return rel_scan_id;
    }

    public String getRel_scan_name() {
        return rel_scan_name;
    }

    public String getRel_scan_virus_name() {
        return rel_scan_virus_name;
    }

    public String getRel_scan_virus_tech() {
        return rel_scan_virus_tech;
    }

    public String getRel_scan_virus_type() {
        return rel_scan_virus_type;
    }

    public String getRel_scan_virus_platform() {
        return rel_scan_virus_platform;
    }

    public String getEset_scan_id() {
        return eset_scan_id;
    }

    public String getEset_scan_name() {
        return eset_scan_name;
    }

    public String getEset_scan_virus_name() {
        return eset_scan_virus_name;
    }

    public String getEset_scan_virus_tech() {
        return eset_scan_virus_tech;
    }

    public String getEset_scan_virus_type() {
        return eset_scan_virus_type;
    }

    public String getEset_scan_virus_platform() {
        return eset_scan_virus_platform;
    }

    public String getMs_scan_id() {
        return ms_scan_id;
    }

    public String getMs_scan_name() {
        return ms_scan_name;
    }

    public String getMs_scan_virus_name() {
        return ms_scan_virus_name;
    }

    public String getMs_scan_virus_tech() {
        return ms_scan_virus_tech;
    }

    public String getMs_scan_virus_type() {
        return ms_scan_virus_type;
    }

    public String getMs_scan_virus_platform() {
        return ms_scan_virus_platform;
    }

    public String getEmu_scan_id() {
        return emu_scan_id;
    }

    public String getEmu_scan_name() {
        return emu_scan_name;
    }

    public String getEmu_scan_virus_name() {
        return emu_scan_virus_name;
    }

    public String getEmu_scan_virus_tech() {
        return emu_scan_virus_tech;
    }

    public String getEmu_scan_virus_type() {
        return emu_scan_virus_type;
    }

    public String getEmu_scan_virus_platform() {
        return emu_scan_virus_platform;
    }

    public String getAvp_scan_id() {
        return avp_scan_id;
    }

    public String getAvp_scan_name() {
        return avp_scan_name;
    }

    public String getAvp_scan_virus_name() {
        return avp_scan_virus_name;
    }

    public String getAvp_scan_virus_tech() {
        return avp_scan_virus_tech;
    }

    public String getAvp_scan_virus_type() {
        return avp_scan_virus_type;
    }

    public String getAvp_scan_virus_platform() {
        return avp_scan_virus_platform;
    }

    public String getTroj_scan_id() {
        return troj_scan_id;
    }

    public String getTroj_scan_name() {
        return troj_scan_name;
    }

    public String getTroj_scan_virus_name() {
        return troj_scan_virus_name;
    }

    public String getTroj_scan_virus_tech() {
        return troj_scan_virus_tech;
    }

    public String getTroj_scan_virus_type() {
        return troj_scan_virus_type;
    }

    public String getTroj_scan_virus_platform() {
        return troj_scan_virus_platform;
    }

    public String getDie_scan_id() {
        return die_scan_id;
    }

    public String getDie_scan_name() {
        return die_scan_name;
    }

    public String getDie_scan_virus_name() {
        return die_scan_virus_name;
    }

    public String getDie_scan_virus_tech() {
        return die_scan_virus_tech;
    }

    public String getDie_scan_virus_type() {
        return die_scan_virus_type;
    }

    public String getDie_scan_virus_platform() {
        return die_scan_virus_platform;
    }

    public String getQvm_scan_id() {
        return qvm_scan_id;
    }

    public String getQvm_scan_name() {
        return qvm_scan_name;
    }

    public String getQvm_scan_virus_name() {
        return qvm_scan_virus_name;
    }

    public String getQvm_scan_virus_tech() {
        return qvm_scan_virus_tech;
    }

    public String getQvm_scan_virus_type() {
        return qvm_scan_virus_type;
    }

    public String getQvm_scan_virus_platform() {
        return qvm_scan_virus_platform;
    }
}
