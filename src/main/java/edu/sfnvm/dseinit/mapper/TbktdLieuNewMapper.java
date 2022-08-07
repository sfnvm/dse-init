package edu.sfnvm.dseinit.mapper;

import edu.sfnvm.dseinit.model.TbktdLieuMgr;
import edu.sfnvm.dseinit.model.TbktdLieuNew;
import org.mapstruct.Mapper;

@Mapper
public interface TbktdLieuNewMapper {
  TbktdLieuNew map(TbktdLieuMgr tbktdLieuMgr);
}
