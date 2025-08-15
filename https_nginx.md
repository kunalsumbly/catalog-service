cat > /home/ec2-user/nginx-ssl-setup-guide.md << 'EOF'
# Nginx SSL/TLS Certificate Setup and Test Server Configuration Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [SSL Certificate Types](#ssl-certificate-types)
4. [Self-Signed Certificates Setup](#self-signed-certificates-setup)
5. [Let's Encrypt Certificates Setup](#lets-encrypt-certificates-setup)
6. [Test Server Configuration (test-server.fake)](#test-server-configuration-test-serverfake)
7. [Nginx Configuration](#nginx-configuration)
8. [Testing and Verification](#testing-and-verification)
9. [Security Best Practices](#security-best-practices)
10. [Troubleshooting](#troubleshooting)

## Overview

This guide provides comprehensive instructions for setting up SSL/TLS certificates in Nginx and configuring a test server with a fake domain (`test-server.fake`) for development and testing purposes.

## Prerequisites

- Amazon Linux EC2 instance with Nginx installed
- Root or sudo access
- Basic understanding of SSL/TLS concepts
- Nginx version 1.28.0 (with SSL support enabled)

## SSL Certificate Types

### 1. Self-Signed Certificates
- Best for: Development, testing, internal services
- Pros: Free, quick setup, full control
- Cons: Browser warnings, not trusted by default

### 2. Let's Encrypt Certificates
- Best for: Production environments with real domains
- Pros: Free, trusted by browsers, automated renewal
- Cons: Requires public domain, rate limits

### 3. Commercial Certificates
- Best for: Enterprise production environments
- Pros: Extended validation options, wildcard support
- Cons: Cost, manual renewal process

## Self-Signed Certificates Setup

### Step 1: Create Certificate Directory
```bash
sudo mkdir -p /etc/nginx/ssl
sudo chmod 700 /etc/nginx/ssl
```

### Step 2: Generate Private Key
```bash
sudo openssl genrsa -out /etc/nginx/ssl/test-server.fake.key 4096
```

### Step 3: Create Certificate Signing Request (CSR)
```bash
sudo openssl req -new -key /etc/nginx/ssl/test-server.fake.key -out /etc/nginx/ssl/test-server.fake.csr
```

When prompted, enter the following information:
- **Country Name**: US
- **State**: Your state
- **City**: Your city
- **Organization**: Your organization
- **Organizational Unit**: IT Department
- **Common Name**: `test-server.fake` (CRITICAL - must match your domain)
- **Email**: your-email@domain.com
- **Challenge password**: (leave blank)
- **Optional company name**: (leave blank)

### Step 4: Generate Self-Signed Certificate
```bash
sudo openssl x509 -req -days 365 -in /etc/nginx/ssl/test-server.fake.csr -signkey /etc/nginx/ssl/test-server.fake.key -out /etc/nginx/ssl/test-server.fake.crt
```

### Step 5: Create Certificate with SAN (Subject Alternative Names)
For better compatibility, create a certificate with SAN:

```bash
# Create a config file for the certificate
sudo cat > /etc/nginx/ssl/test-server.fake.conf << 'CERT_EOF'
[req]
default_bits = 4096
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = v3_req

[dn]
CN=test-server.fake
emailAddress=admin@test-server.fake
O=Test Organization
L=Test City
ST=Test State
C=US

[v3_req]
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = test-server.fake
DNS.2 = *.test-server.fake
DNS.3 = localhost
IP.1 = 127.0.0.1
IP.2 = ::1
CERT_EOF

# Generate the certificate with SAN
sudo openssl req -new -x509 -key /etc/nginx/ssl/test-server.fake.key -out /etc/nginx/ssl/test-server.fake.crt -days 365 -config /etc/nginx/ssl/test-server.fake.conf -extensions v3_req
```

### Step 6: Set Proper Permissions
```bash
sudo chmod 600 /etc/nginx/ssl/test-server.fake.key
sudo chmod 644 /etc/nginx/ssl/test-server.fake.crt
sudo chown nginx:nginx /etc/nginx/ssl/test-server.fake.*
```

## Let's Encrypt Certificates Setup

### Step 1: Install Certbot
```bash
# For Amazon Linux
sudo yum install -y certbot python3-certbot-nginx

# Or install via pip
sudo pip3 install certbot certbot-nginx
```

### Step 2: Obtain Certificate
```bash
# For a real domain (replace with your actual domain)
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# Or manually obtain certificate
sudo certbot certonly --nginx -d your-domain.com
```

### Step 3: Set Up Auto-Renewal
```bash
# Add to crontab
sudo crontab -e

# Add this line for automatic renewal
0 12 * * * /usr/bin/certbot renew --quiet
```

## Test Server Configuration (test-server.fake)

### Step 1: Configure Local DNS Resolution

#### Method 1: Hosts File (Local Testing)
```bash
# Add to /etc/hosts
echo "127.0.0.1 test-server.fake" | sudo tee -a /etc/hosts
echo "::1 test-server.fake" | sudo tee -a /etc/hosts
```

#### Method 2: Local DNS Server (Advanced)
```bash
# Install dnsmasq
sudo yum install -y dnsmasq

# Configure dnsmasq
sudo cat > /etc/dnsmasq.d/test-server.conf << 'DNS_EOF'
address=/test-server.fake/127.0.0.1
address=/test-server.fake/::1
DNS_EOF

# Start and enable dnsmasq
sudo systemctl start dnsmasq
sudo systemctl enable dnsmasq
```

### Step 2: Create Test Application
```bash
# Create a simple test application directory
sudo mkdir -p /var/www/test-server.fake/html
sudo mkdir -p /var/log/nginx/test-server.fake

# Create a simple index page
sudo cat > /var/www/test-server.fake/html/index.html << 'HTML_EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test Server - SSL/TLS Enabled</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; }
        .success { color: green; font-weight: bold; }
        .info { background: #f0f8ff; padding: 20px; border-radius: 5px; }
    </style>
</head>
<body>
    <h1>🔒 SSL/TLS Test Server</h1>
    <p class="success">✅ HTTPS is working correctly!</p>
    <div class="info">
        <h3>Server Information:</h3>
        <ul>
            <li><strong>Domain:</strong> test-server.fake</li>
            <li><strong>Protocol:</strong> HTTPS</li>
            <li><strong>Certificate Type:</strong> Self-Signed</li>
            <li><strong>Server:</strong> Nginx</li>
        </ul>
    </div>
    <h3>Test Endpoints:</h3>
    <ul>
        <li><a href="/api/status">API Status</a></li>
        <li><a href="/secure">Secure Area</a></li>
        <li><a href="/health">Health Check</a></li>
    </ul>
</body>
</html>
HTML_EOF

# Create additional test endpoints
sudo mkdir -p /var/www/test-server.fake/html/api
sudo cat > /var/www/test-server.fake/html/api/status << 'JSON_EOF'
{
  "status": "healthy",
  "ssl": "enabled",
  "timestamp": "$(date -Iseconds)",
  "server": "nginx"
}
JSON_EOF

# Set permissions
sudo chown -R nginx:nginx /var/www/test-server.fake
sudo chmod -R 755 /var/www/test-server.fake
```

## Nginx Configuration

### Step 1: Create Server Block Configuration
```bash
sudo cat > /etc/nginx/conf.d/test-server.fake.conf << 'NGINX_EOF'
# HTTP to HTTPS redirect
server {
    listen 80;
    server_name test-server.fake;
    
    # Redirect all HTTP requests to HTTPS
    return 301 https://$server_name$request_uri;
}

# HTTPS server block
server {
    listen 443 ssl http2;
    server_name test-server.fake;
    
    # Document root
    root /var/www/test-server.fake/html;
    index index.html index.htm;
    
    # SSL Certificate Configuration
    ssl_certificate /etc/nginx/ssl/test-server.fake.crt;
    ssl_certificate_key /etc/nginx/ssl/test-server.fake.key;
    
    # SSL Security Configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    ssl_session_tickets off;
    
    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Referrer-Policy "strict-origin-when-cross-origin";
    
    # Logging
    access_log /var/log/nginx/test-server.fake/access.log;
    error_log /var/log/nginx/test-server.fake/error.log;
    
    # Main location
    location / {
        try_files $uri $uri/ =404;
    }
    
    # API endpoints
    location /api/ {
        add_header Content-Type application/json;
    }
    
    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
    
    # Security location (example of protected area)
    location /secure {
        # Add authentication if needed
        # auth_basic "Restricted Area";
        # auth_basic_user_file /etc/nginx/.htpasswd;
        
        try_files $uri $uri/ =404;
    }
    
    # Deny access to hidden files
    location ~ /\. {
        deny all;
    }
}
NGINX_EOF
```

### Step 2: Test Configuration and Reload
```bash
# Test nginx configuration
sudo nginx -t

# If configuration is valid, reload nginx
sudo systemctl reload nginx

# Ensure nginx is running and enabled
sudo systemctl status nginx
sudo systemctl enable nginx
```

## Testing and Verification

### Step 1: Basic Connectivity Tests
```bash
# Test HTTP redirect
curl -I http://test-server.fake

# Test HTTPS (ignore certificate warnings for self-signed)
curl -k https://test-server.fake

# Test with certificate verification disabled
curl --insecure -v https://test-server.fake
```

### Step 2: SSL Certificate Verification
```bash
# Check certificate details
openssl s_client -connect test-server.fake:443 -servername test-server.fake < /dev/null

# Check certificate expiration
echo | openssl s_client -connect test-server.fake:443 -servername test-server.fake 2>/dev/null | openssl x509 -noout -dates

# Verify certificate chain
openssl verify /etc/nginx/ssl/test-server.fake.crt
```

### Step 3: Browser Testing
1. Open browser and navigate to `https://test-server.fake`
2. Accept the security warning (for self-signed certificates)
3. Verify the SSL/TLS connection in browser developer tools
4. Check that HTTP redirects to HTTPS

### Step 4: SSL Labs Testing (for real domains)
```bash
# For real domains, test with SSL Labs
# https://www.ssllabs.com/ssltest/
```

## Security Best Practices

### 1. Certificate Security
- Use strong key lengths (4096-bit RSA or 256-bit ECC)
- Store private keys securely with proper permissions (600)
- Regularly rotate certificates
- Use certificate pinning for critical applications

### 2. SSL/TLS Configuration
```nginx
# Modern SSL configuration
ssl_protocols TLSv1.2 TLSv1.3;
ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384;
ssl_prefer_server_ciphers off;
ssl_session_cache shared:SSL:10m;
ssl_session_timeout 10m;
ssl_session_tickets off;

# OCSP stapling (for real certificates)
ssl_stapling on;
ssl_stapling_verify on;
ssl_trusted_certificate /path/to/chain.crt;
resolver 8.8.8.8 8.8.4.4 valid=300s;
resolver_timeout 5s;
```

### 3. Security Headers
```nginx
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
add_header X-Frame-Options DENY;
add_header X-Content-Type-Options nosniff;
add_header X-XSS-Protection "1; mode=block";
add_header Referrer-Policy "strict-origin-when-cross-origin";
add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';";
```

### 4. Monitoring and Logging
```bash
# Monitor certificate expiration
*/0 1 * * * /usr/local/bin/check-cert-expiry.sh

# Monitor SSL/TLS connections
tail -f /var/log/nginx/test-server.fake/access.log | grep -E "(GET|POST|PUT|DELETE)"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Certificate Not Trusted
**Problem**: Browser shows "Not Secure" or certificate warnings
**Solutions**:
- For self-signed: Add certificate to browser/system trust store
- For Let's Encrypt: Ensure domain is publicly accessible
- Check certificate chain completeness

#### 2. SSL Handshake Failures
**Problem**: SSL connection fails
**Solutions**:
```bash
# Check SSL configuration
nginx -t

# Verify certificate and key match
openssl rsa -noout -modulus -in /etc/nginx/ssl/test-server.fake.key | openssl md5
openssl x509 -noout -modulus -in /etc/nginx/ssl/test-server.fake.crt | openssl md5

# Check supported protocols and ciphers
nmap --script ssl-enum-ciphers -p 443 test-server.fake
```

#### 3. Mixed Content Issues
**Problem**: Some resources load over HTTP on HTTPS pages
**Solutions**:
- Ensure all resources use HTTPS URLs
- Use protocol-relative URLs (`//example.com/resource`)
- Configure proper redirect rules

#### 4. Performance Issues
**Problem**: SSL/TLS causing slow page loads
**Solutions**:
- Enable HTTP/2
- Configure SSL session caching
- Use OCSP stapling
- Optimize cipher suites

### Debugging Commands
```bash
# Check nginx error logs
sudo tail -f /var/log/nginx/error.log

# Check SSL-specific errors
sudo journalctl -u nginx.service -f

# Test specific cipher suites
openssl s_client -connect test-server.fake:443 -cipher 'ECDHE-RSA-AES256-GCM-SHA384'

# Verify DNS resolution
nslookup test-server.fake
dig test-server.fake
```

### Log Analysis
```bash
# Analyze SSL handshake errors
grep "SSL" /var/log/nginx/error.log

# Monitor certificate-related errors
grep -i "certificate\|ssl\|tls" /var/log/nginx/error.log

# Check access patterns
awk '{print $1}' /var/log/nginx/test-server.fake/access.log | sort | uniq -c | sort -nr
```

## Maintenance Tasks

### Monthly Tasks
- [ ] Check certificate expiration dates
- [ ] Review SSL/TLS security configurations
- [ ] Analyze access logs for anomalies
- [ ] Update nginx and OpenSSL if needed

### Quarterly Tasks
- [ ] Review and update cipher suites
- [ ] Test disaster recovery procedures
- [ ] Audit certificate inventory
- [ ] Performance optimization review

### Yearly Tasks
- [ ] Renew self-signed certificates
- [ ] Security configuration audit
- [ ] Review and update documentation
- [ ] Test backup and restore procedures

---

**Generated on**: $(date)
**System**: Amazon Linux with Nginx 1.28.0
**Author**: AI Assistant
**Version**: 1.0

This guide provides a comprehensive reference for SSL/TLS certificate setup in Nginx with a focus on the test-server.fake configuration for development and testing purposes.
EOF