package edu.sfnvm.dseinit.mapper;

import edu.sfnvm.dseinit.dto.TbOneDto;
import edu.sfnvm.dseinit.model.TbOne;
import org.mapstruct.Mapper;

@Mapper
public interface TbOneMapper {
    TbOneDto map(TbOne tbOne);

    TbOne map(TbOneDto tbOneDto);
}
