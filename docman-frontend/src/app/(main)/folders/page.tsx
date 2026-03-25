'use client';

import { useState } from 'react';
import { Button, Input, Space, Card, Row, Col, Modal, Form, Select, message, Typography } from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  FolderOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import { useRouter } from 'next/navigation';
import { useFolders, useCategories } from '@/hooks/useDocument';
import { documentService } from '@/services/document';

const { Title } = Typography;

export default function FoldersPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [selectedFolder, setSelectedFolder] = useState<any>(null);
  const [form] = Form.useForm();
  const [editForm] = Form.useForm();

  const { folders, isLoading, mutate } = useFolders();
  const { categories } = useCategories();

  const handleCreate = async (values: { name: string; parentId?: string }) => {
    try {
      await documentService.createFolder(values);
      message.success(t('common.success'));
      setCreateModalVisible(false);
      form.resetFields();
      mutate();
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    }
  };

  const handleEdit = async (values: { name: string }) => {
    if (!selectedFolder) return;

    try {
      await documentService.updateFolder(selectedFolder.id, values);
      message.success(t('common.success'));
      setEditModalVisible(false);
      setSelectedFolder(null);
      editForm.resetFields();
      mutate();
    } catch (error: any) {
      message.error(error.message || t('common.error'));
    }
  };

  const handleDelete = (folder: any) => {
    Modal.confirm({
      title: t('folders.deleteConfirm'),
      onOk: async () => {
        try {
          await documentService.deleteFolder(folder.id);
          message.success(t('common.success'));
          mutate();
        } catch (error: any) {
          message.error(error.message || t('common.error'));
        }
      },
    });
  };

  const openEditModal = (folder: any) => {
    setSelectedFolder(folder);
    editForm.setFieldsValue({ name: folder.name });
    setEditModalVisible(true);
  };

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Title level={3} style={{ margin: 0 }}>{t('folders.title')}</Title>
          </Col>
          <Col>
            <Space>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setCreateModalVisible(true)}
              >
                {t('folders.createFolder')}
              </Button>
            </Space>
          </Col>
        </Row>
      </div>

      <Card style={{ marginBottom: 16 }}>
        <Input
          placeholder={t('search.placeholder')}
          prefix={<SearchOutlined />}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          allowClear
        />
      </Card>

      <Card>
        {folders.length === 0 && !isLoading ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
            {t('folders.noFolders')}
          </div>
        ) : (
          <div>
            {folders.map((folder) => (
              <Card.Grid
                key={folder.id}
                style={{ width: '25%', padding: '16px', cursor: 'pointer' }}
                onClick={() => router.push(`/documents?folder=${folder.id}`)}
              >
                <Space direction="vertical" style={{ width: '100%' }} size="small">
                  <FolderOutlined style={{ fontSize: 48, color: '#1890ff' }} />
                  <Typography.Text strong>{folder.name}</Typography.Text>
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {folder.documentCount} documents
                  </Typography.Text>
                  <Space>
                    <Button
                      type="text"
                      size="small"
                      icon={<EditOutlined />}
                      onClick={(e) => {
                        e.stopPropagation();
                        openEditModal(folder);
                      }}
                    />
                    <Button
                      type="text"
                      size="small"
                      danger
                      icon={<DeleteOutlined />}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDelete(folder);
                      }}
                    />
                  </Space>
                </Space>
              </Card.Grid>
            ))}
          </div>
        )}
      </Card>

      <Modal
        title={t('folders.createFolder')}
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
          form.resetFields();
        }}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="name"
            label={t('folders.folderName')}
            rules={[{ required: true, message: 'Please enter folder name' }]}
          >
            <Input placeholder={t('folders.folderName')} />
          </Form.Item>

          <Form.Item name="parentId" label={t('folders.parentFolder')}>
            <Select
              placeholder={t('folders.parentFolder')}
              allowClear
              options={folders.map((f) => ({ label: f.name, value: f.id }))}
            />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setCreateModalVisible(false)}>
                {t('common.cancel')}
              </Button>
              <Button type="primary" htmlType="submit">
                {t('common.create')}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={t('common.edit')}
        open={editModalVisible}
        onCancel={() => {
          setEditModalVisible(false);
          setSelectedFolder(null);
          editForm.resetFields();
        }}
        footer={null}
      >
        <Form form={editForm} layout="vertical" onFinish={handleEdit}>
          <Form.Item
            name="name"
            label={t('folders.folderName')}
            rules={[{ required: true, message: 'Please enter folder name' }]}
          >
            <Input placeholder={t('folders.folderName')} />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setEditModalVisible(false)}>
                {t('common.cancel')}
              </Button>
              <Button type="primary" htmlType="submit">
                {t('common.save')}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
