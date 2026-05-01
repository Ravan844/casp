output "resource_group_name" {
  value = module.network.resource_group_name
}

output "vnet_name" {
  value = module.network.vnet_name
}

output "frontend_private_ip" {
  value = module.compute.frontend_private_ip
}

output "backend_private_ip" {
  value = module.compute.backend_private_ip
}

output "ops_private_ip" {
  value = module.compute.ops_private_ip
}

output "ops_public_ip" {
  value = module.compute.ops_public_ip
}
