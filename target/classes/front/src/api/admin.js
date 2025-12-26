import request from '@/utils/request'

// 获取所有管理员
export function getAdminList() {
  return request({
    url: '/user/admin/list',
    method: 'get'
  })
}

// 创建管理员
export function createAdmin(data) {
  return request({
    url: '/user/admin/add',
    method: 'post',
    data: data
  })
}

// 更新管理员信息
export function updateAdmin(id, data) {
  return request({
    url: `/user/admin/${id}`,
    method: 'put',
    data: data
  })
}

// 删除管理员
export function deleteAdmin(id) {
  return request({
    url: `/user/admin/${id}`,
    method: 'delete'
  })
}

// 获取管理员详情
export function getAdminDetail(id) {
  return request({
    url: `/user/admin/${id}`,
    method: 'get'
  })
}