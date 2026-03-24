'use client';

import { useState } from 'react';
import { Form, Input, Button, Card, message, Typography, Space } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/hooks/useAuth';

const { Title, Text } = Typography;

export default function RegisterPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { register, isLoading } = useAuth();
  const [form] = Form.useForm();

  const handleSubmit = async (values: {
    email: string;
    username: string;
    password: string;
    confirmPassword: string;
  }) => {
    if (values.password !== values.confirmPassword) {
      message.error(t('auth.passwordMismatch'));
      return;
    }

    if (values.password.length < 6) {
      message.error(t('auth.passwordTooShort'));
      return;
    }

    try {
      await register(values.email, values.username, values.password);
      message.success(t('auth.registerSuccess'));
      router.push('/documents');
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <Card style={{ width: 400, boxShadow: '0 14px 45px rgba(0,0,0,0.2)' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={3}>{t('auth.registerTitle')}</Title>
        </div>

        <Form
          form={form}
          name="register"
          onFinish={handleSubmit}
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="email"
            rules={[
              { required: true, message: t('auth.emailRequired') },
              { type: 'email', message: 'Please enter a valid email' },
            ]}
          >
            <Input
              prefix={<MailOutlined />}
              placeholder={t('common.email')}
            />
          </Form.Item>

          <Form.Item
            name="username"
            rules={[{ required: true, message: t('auth.usernameRequired') }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder={t('common.username')}
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: t('auth.passwordRequired') }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder={t('common.password')}
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            rules={[{ required: true, message: t('auth.passwordRequired') }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder={t('common.confirmPassword')}
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={isLoading}
            >
              {t('common.register')}
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <Space>
              <Text>{t('common.hasAccount')}</Text>
              <Link href="/login">{t('common.login')}</Link>
            </Space>
          </div>
        </Form>
      </Card>
    </div>
  );
}
