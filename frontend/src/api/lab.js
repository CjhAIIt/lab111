import request from '@/utils/request'

// 获取所有实验室
export function getAllLabs() {
  return request({
    url: '/api/labs/list',
    method: 'get',
    params: {
      pageNum: 1,
      pageSize: 100  // 获取足够多的实验室数据
    }
  })
}

// 获取实验室详情
export function getLabById(id) {
  return request({
    url: `/api/labs/${id}`,
    method: 'get'
  })
}

// 添加实验室
export function addLab(data) {
  return request({
    url: '/api/labs',
    method: 'post',
    data
  })
}

// 更新实验室信息
export function updateLab(data) {
  return request({
    url: `/api/labs/${data.id}`,
    method: 'put',
    data
  })
}

// 删除实验室
export function deleteLab(id) {
  return request({
    url: `/api/labs/${id}`,
    method: 'delete'
  })
}