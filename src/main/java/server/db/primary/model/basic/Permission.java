package server.db.primary.model.basic;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.CacheNamespace;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@Entity
public class Permission  implements Serializable {
//    权限的type，parentId，isEnable，value，note等属性暂未添加
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String permission;
}