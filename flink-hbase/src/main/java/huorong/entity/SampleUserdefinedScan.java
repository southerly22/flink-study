package huorong.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author lzx
 * @date 2023/6/5 17:07
 * @description: TODO
 */
@Data
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
}
