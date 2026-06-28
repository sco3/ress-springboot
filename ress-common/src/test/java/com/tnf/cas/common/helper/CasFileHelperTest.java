package com.tnf.cas.common.helper;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.tnf.cas.common.helper.CasFileHelper.FileRemoverInfo;

import junit.framework.Assert;

public class CasFileHelperTest {
    @Test
    public void test() throws Exception {
        List<FileRemoverInfo> list = new ArrayList<FileRemoverInfo>();
        list.add(new FileRemoverInfo(Paths.get("p2"), 2L));
        list.add(new FileRemoverInfo(Paths.get("p3"), 3L));
        list.add(new FileRemoverInfo(Paths.get("p1"), 1L));
        CasFileHelper.sortByLastModifiedTimeDescending(list);
        Assert.assertEquals("p3", list.get(0).getPath().getFileName().toString());
        Assert.assertEquals("p2", list.get(1).getPath().getFileName().toString());
        Assert.assertEquals("p1", list.get(2).getPath().getFileName().toString());
    }
}
